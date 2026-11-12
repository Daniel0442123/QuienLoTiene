import { screen, within } from '@testing-library/react'
import userEvent, { type UserEvent } from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { FormularioRegistroCliente } from './FormularioRegistroCliente'

const RUTA_REGISTRO = '/api/v1/auth/registro'

interface Datos {
  nombre?: string
  correo?: string
  telefono?: string
  frase?: string
  acepta?: boolean
}

async function llenar(usuario: UserEvent, datos: Datos = {}) {
  const {
    nombre = 'Ana Pérez',
    correo = 'ana@correo.com',
    telefono = '300 123 4567',
    frase = 'mi perro come arepa los domingos',
    acepta = true,
  } = datos
  if (nombre) await usuario.type(screen.getByRole('textbox', { name: 'Nombre' }), nombre)
  if (correo) await usuario.type(screen.getByRole('textbox', { name: 'Correo' }), correo)
  if (telefono) await usuario.type(screen.getByRole('textbox', { name: 'Celular' }), telefono)
  if (frase) await usuario.type(screen.getByLabelText('Frase de contraseña'), frase)
  if (acepta) await usuario.click(screen.getByRole('checkbox', { name: /Autorizo el tratamiento/ }))
}

const botonCrear = () => screen.getByRole('button', { name: 'Crear cuenta' })

describe('HU-001 · HU-005 · Registro de cliente', () => {
  it('no tiene violaciones de accesibilidad, ni vacío ni con errores', async () => {
    // Dado
    const usuario = userEvent.setup()
    const { container } = renderizarConProveedores(<FormularioRegistroCliente alRegistrar={vi.fn()} />)
    expect(await violacionesDeAccesibilidad(container)).toEqual([])

    // Cuando
    await usuario.click(botonCrear())

    // Entonces
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-IDE-001-01 · crea la cuenta y avisa con el correo registrado', async () => {
    // Dado
    const usuario = userEvent.setup()
    const alRegistrar = vi.fn()
    renderizarConProveedores(<FormularioRegistroCliente alRegistrar={alRegistrar} />)

    // Cuando
    await llenar(usuario)
    await usuario.click(botonCrear())

    // Entonces
    await vi.waitFor(() => expect(alRegistrar).toHaveBeenCalledWith('ana@correo.com'))
    const creado = base.usuarios.find((u) => u.correo === 'ana@correo.com')
    expect(creado).toMatchObject({ rol: 'CLIENTE', telefono: '3001234567' })
  })

  it('CP-IDE-001-02 · con un correo que ya tiene cuenta lo marca y ofrece entrar', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioRegistroCliente alRegistrar={vi.fn()} />)

    // Cuando
    await llenar(usuario, { correo: CORREOS_DE_PRUEBA.cliente })
    await usuario.click(botonCrear())

    // Entonces
    // (la región de anuncios urgentes también es role="alert": se busca el aviso por su texto)
    const aviso = (await screen.findByText(/Ya existe una cuenta con ese correo/)).closest('[role="alert"]')
    expect(aviso).not.toBeNull()
    expect(within(aviso as HTMLElement).getByRole('link', { name: 'Entrar con ese correo' })).toHaveAttribute(
      'href',
      '/entrar',
    )
    const campoCorreo = screen.getByRole('textbox', { name: 'Correo' })
    expect(campoCorreo).toHaveAttribute('aria-invalid', 'true')
    expect(campoCorreo).toHaveFocus()
  })

  it('CP-IDE-001-03 · con una frase de 10 caracteres explica el mínimo y cuántos lleva', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioRegistroCliente alRegistrar={vi.fn()} />)
    const campoFrase = screen.getByLabelText('Frase de contraseña')

    // Cuando
    await usuario.type(campoFrase, 'corta 1234')
    await usuario.tab()

    // Entonces
    expect(campoFrase).toHaveAttribute('aria-invalid', 'true')
    expect(campoFrase).toHaveAccessibleDescription(/al menos 15 caracteres\. Llevas 10\./)
  })

  it('CP-IDE-001-03 · con una frase común que rechaza el backend (IDE-003) marca el campo de la frase', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioRegistroCliente alRegistrar={vi.fn()} />)

    // Cuando
    await llenar(usuario, { frase: 'password password' })
    await usuario.click(botonCrear())

    // Entonces
    const campoFrase = screen.getByLabelText('Frase de contraseña')
    await vi.waitFor(() => expect(campoFrase).toHaveAttribute('aria-invalid', 'true'))
    expect(campoFrase).toHaveAccessibleDescription(/muy común/)
    expect(campoFrase).toHaveFocus()
  })

  it('CP-IDE-005-01 · sin marcar la autorización no envía nada, resalta la casilla y la resume arriba', async () => {
    // Dado
    const usuario = userEvent.setup()
    const peticiones = vi.fn()
    servidorSimulado.events.on('request:start', peticiones)
    renderizarConProveedores(<FormularioRegistroCliente alRegistrar={vi.fn()} />)

    // Cuando
    await llenar(usuario, { acepta: false })
    await usuario.click(botonCrear())

    // Entonces
    const casilla = screen.getByRole('checkbox', { name: /Autorizo el tratamiento/ })
    expect(casilla).toHaveAttribute('aria-invalid', 'true')
    expect(casilla).toHaveAccessibleDescription(/debes marcar la autorización/)
    expect(casilla).toHaveFocus()
    const resumen = screen.getByRole('region', { name: /Revisa estos datos/ })
    expect(within(resumen).getAllByRole('link')).toHaveLength(1)
    expect(peticiones).not.toHaveBeenCalled()
    servidorSimulado.events.removeAllListeners()
  })

  it('CP-IDE-005-02 · envía la autorización con la versión 1.0 de la política', async () => {
    // Dado
    const usuario = userEvent.setup()
    let cuerpo: unknown
    servidorSimulado.use(
      http.post(RUTA_REGISTRO, async ({ request }) => {
        cuerpo = await request.clone().json()
        // Sin respuesta: sigue al manejador normal de la simulación
      }),
    )
    renderizarConProveedores(<FormularioRegistroCliente alRegistrar={vi.fn()} />)

    // Cuando
    await llenar(usuario)
    await usuario.click(botonCrear())

    // Entonces
    await vi.waitFor(() =>
      expect(cuerpo).toMatchObject({ aceptaPoliticaDatos: true, versionPoliticaDatos: '1.0', telefono: '3001234567' }),
    )
  })

  it('deja leer la política de datos antes de aceptarla', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioRegistroCliente alRegistrar={vi.fn()} />)

    // Cuando
    await usuario.click(screen.getByText('Leer la política de tratamiento de datos'))

    // Entonces
    expect(screen.getByRole('heading', { name: /Política de tratamiento de datos · versión 1\.0/ })).toBeVisible()
  })

  it('el enlace del resumen lleva el foco al campo con error', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioRegistroCliente alRegistrar={vi.fn()} />)
    await usuario.click(botonCrear())

    // Cuando
    await usuario.click(screen.getByRole('link', { name: 'Escribe tu correo.' }))

    // Entonces
    expect(screen.getByRole('textbox', { name: 'Correo' })).toHaveFocus()
  })

  it('estado enviando · el botón dice que está creando la cuenta y no permite otro envío', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(http.post(RUTA_REGISTRO, () => delay('infinite')))
    renderizarConProveedores(<FormularioRegistroCliente alRegistrar={vi.fn()} />)

    // Cuando
    await llenar(usuario)
    await usuario.click(botonCrear())

    // Entonces
    const boton = await screen.findByRole('button', { name: 'Creando cuenta…' })
    expect(boton).toBeDisabled()
  })

  it('estado error · un fallo del servidor muestra qué pasó y el código para soporte', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(
      http.post(RUTA_REGISTRO, () =>
        HttpResponse.json(
          { title: 'Error interno', status: 500, codigo: 'GEN-008', idCorrelacion: 'abc-123', errores: [] },
          { status: 500 },
        ),
      ),
    )
    renderizarConProveedores(<FormularioRegistroCliente alRegistrar={vi.fn()} />)

    // Cuando
    await llenar(usuario)
    await usuario.click(botonCrear())

    // Entonces
    const aviso = (await screen.findByText(/Algo falló de nuestro lado/)).closest('[role="alert"]')
    expect(aviso).toHaveTextContent('Código para soporte: abc-123')
    expect(botonCrear()).toBeEnabled()
  })
})
