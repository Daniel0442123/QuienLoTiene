import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { CODIGO_TOTP_DE_PRUEBA, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion, obtenerSesion } from '@/services/sesion'
import { FormularioSegundoFactor } from './FormularioSegundoFactor'

const campoCodigo = () => screen.getByRole('textbox', { name: 'Código de 6 dígitos' })
const botonVerificar = () => screen.getByRole('button', { name: 'Verificar' })

/** La región de anuncios urgentes también es role="alert": el aviso se busca por su texto. */
async function avisoCon(texto: RegExp) {
  return (await screen.findByText(texto)).closest('[role="alert"]')
}

describe('HU-004 · Segundo factor del administrador', () => {
  beforeEach(async () => {
    limpiarSesion()
    // Dado: el administrador ya escribió bien su correo y su frase (CU-25 pasos 1 a 6)
    await iniciarSesion(CORREOS_DE_PRUEBA.administrador, FRASE_DE_PRUEBA)
  })

  it('no tiene violaciones de accesibilidad, ni vacío ni con error', async () => {
    // Dado
    const usuario = userEvent.setup()
    const { container } = renderizarConProveedores(<FormularioSegundoFactor alVerificar={vi.fn()} />)
    expect(await violacionesDeAccesibilidad(container)).toEqual([])

    // Cuando
    await usuario.click(botonVerificar())

    // Entonces
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('el campo abre el teclado numérico y acepta el código del gestor (one-time-code)', () => {
    // Dado
    renderizarConProveedores(<FormularioSegundoFactor alVerificar={vi.fn()} />)

    // Entonces
    expect(campoCodigo()).toHaveAttribute('inputmode', 'numeric')
    expect(campoCodigo()).toHaveAttribute('autocomplete', 'one-time-code')
  })

  it('CP-IDE-004-01 · con un código vigente entrega la sesión de administrador', async () => {
    // Dado
    const usuario = userEvent.setup()
    const alVerificar = vi.fn()
    renderizarConProveedores(<FormularioSegundoFactor alVerificar={alVerificar} />)

    // Cuando
    await usuario.type(campoCodigo(), CODIGO_TOTP_DE_PRUEBA)
    await usuario.click(botonVerificar())

    // Entonces
    await vi.waitFor(() => expect(alVerificar).toHaveBeenCalledWith(expect.objectContaining({ rol: 'ADMIN' })))
    expect(obtenerSesion()?.rol).toBe('ADMIN')
  })

  it('acepta el código escrito con espacio, como lo muestran algunas apps', async () => {
    // Dado
    const usuario = userEvent.setup()
    const alVerificar = vi.fn()
    renderizarConProveedores(<FormularioSegundoFactor alVerificar={alVerificar} />)

    // Cuando
    await usuario.type(campoCodigo(), '123 456')
    await usuario.click(botonVerificar())

    // Entonces
    await vi.waitFor(() => expect(alVerificar).toHaveBeenCalled())
  })

  it('CP-IDE-004-02 · con un código incorrecto niega el acceso, lo dice en el campo y lo deja listo para otro', async () => {
    // Dado
    const usuario = userEvent.setup()
    const alVerificar = vi.fn()
    renderizarConProveedores(<FormularioSegundoFactor alVerificar={alVerificar} />)

    // Cuando
    await usuario.type(campoCodigo(), '000000')
    await usuario.click(botonVerificar())

    // Entonces
    await vi.waitFor(() => expect(campoCodigo()).toHaveAttribute('aria-invalid', 'true'))
    expect(campoCodigo()).toHaveAccessibleDescription(/no es correcto/)
    expect(campoCodigo()).toHaveValue('')
    expect(campoCodigo()).toHaveFocus()
    expect(alVerificar).not.toHaveBeenCalled()
    expect(obtenerSesion()).toBeNull()
  })

  it('CP-IDE-004-02 · al agotar los intentos explica que venció y ofrece volver a entrar (deshabilitado con razón)', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioSegundoFactor alVerificar={vi.fn()} />)

    // Cuando
    for (let i = 0; i < 6; i++) {
      await usuario.type(campoCodigo(), '000000')
      await usuario.click(botonVerificar())
      await vi.waitFor(() => expect(botonVerificar()).not.toHaveAttribute('aria-busy'))
    }

    // Entonces
    const aviso = await avisoCon(/venció o se agotaron los intentos/)
    expect(aviso).not.toBeNull()
    expect(screen.getByRole('link', { name: 'Volver a entrar' })).toHaveAttribute('href', '/entrar')
    expect(campoCodigo()).toBeDisabled()
    expect(botonVerificar()).toBeDisabled()
    expect(obtenerSesion()).toBeNull()
  })

  it('con 5 dígitos dice cuántos lleva, sin llamar al backend', async () => {
    // Dado
    const usuario = userEvent.setup()
    const peticiones = vi.fn()
    servidorSimulado.events.on('request:start', peticiones)
    renderizarConProveedores(<FormularioSegundoFactor alVerificar={vi.fn()} />)

    // Cuando
    await usuario.type(campoCodigo(), '12345')
    await usuario.click(botonVerificar())

    // Entonces
    expect(campoCodigo()).toHaveAccessibleDescription(/El código tiene 6 dígitos\. Llevas 5\./)
    expect(peticiones).not.toHaveBeenCalled()
    servidorSimulado.events.removeAllListeners()
  })

  it('estado enviando · el botón dice que está verificando y no permite otro envío', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(http.post('/api/v1/auth/totp', () => delay('infinite')))
    renderizarConProveedores(<FormularioSegundoFactor alVerificar={vi.fn()} />)

    // Cuando
    await usuario.type(campoCodigo(), CODIGO_TOTP_DE_PRUEBA)
    await usuario.click(botonVerificar())

    // Entonces
    expect(await screen.findByRole('button', { name: 'Verificando…' })).toBeDisabled()
  })

  it('estado error · un fallo del servidor muestra qué pasó y el código para soporte', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(
      http.post('/api/v1/auth/totp', () =>
        HttpResponse.json(
          { title: 'Error interno', status: 500, codigo: 'GEN-008', idCorrelacion: 'abc-123', errores: [] },
          { status: 500 },
        ),
      ),
    )
    renderizarConProveedores(<FormularioSegundoFactor alVerificar={vi.fn()} />)

    // Cuando
    await usuario.type(campoCodigo(), CODIGO_TOTP_DE_PRUEBA)
    await usuario.click(botonVerificar())

    // Entonces
    const aviso = await avisoCon(/Algo falló de nuestro lado/)
    expect(aviso).toHaveTextContent('Código para soporte: abc-123')
    expect(botonVerificar()).toBeEnabled()
  })
})
