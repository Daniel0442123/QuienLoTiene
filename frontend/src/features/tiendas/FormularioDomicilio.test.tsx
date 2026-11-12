import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { FormularioDomicilio } from './FormularioDomicilio'
import { SimuladorDomicilio } from './SimuladorDomicilio'

const conDomicilio = { haceDomicilio: true, radioCoberturaEnMetros: 3000, tipoTarifa: 'FIJA', tarifaFijaEnPesos: 4000 }
const sinDomicilio = { haceDomicilio: false, radioCoberturaEnMetros: null, tipoTarifa: null, tarifaFijaEnPesos: null }
const domicilioGuardado = () => base.tiendas.find((t) => t.estado === 'APROBADA')?.domicilio
const campoTarifa = () => screen.getByRole('textbox', { name: 'Valor del domicilio (pesos)' })
const botonGuardar = () => screen.getByRole('button', { name: 'Guardar cambios' })

describe('HU-010 · Domicilio con tarifa fija', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA)
  })

  it('no tiene violaciones de accesibilidad detectables', async () => {
    const { container } = renderizarConProveedores(<FormularioDomicilio domicilio={conDomicilio} />)
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-TIE-010-01 · guarda la tarifa fija y muestra cómo la verán los clientes', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioDomicilio domicilio={conDomicilio} />)

    // Cuando
    await usuario.clear(campoTarifa())
    await usuario.type(campoTarifa(), '5.000')
    await usuario.selectOptions(screen.getByRole('combobox', { name: /Hasta qué distancia/ }), '5000')
    await usuario.click(botonGuardar())

    // Entonces
    expect(campoTarifa()).toHaveAccessibleDescription(/Los clientes verán: \$\s5\.000/)
    await vi.waitFor(() =>
      expect(domicilioGuardado()).toEqual({
        haceDomicilio: true,
        radioCoberturaEnMetros: 5000,
        tarifaFijaEnPesos: 5000,
      }),
    )
    expect(await screen.findByText('Cambios guardados.', { selector: 'p.text-exito' })).toBeInTheDocument()
  })

  it('CP-TIE-010-02 · no acepta una tarifa negativa', async () => {
    // Dado
    const usuario = userEvent.setup()
    const peticiones = vi.fn()
    servidorSimulado.events.on('request:start', ({ request }) => {
      if (request.method === 'PUT') peticiones()
    })
    renderizarConProveedores(<FormularioDomicilio domicilio={conDomicilio} />)

    // Cuando
    await usuario.clear(campoTarifa())
    await usuario.type(campoTarifa(), '-500')
    await usuario.click(botonGuardar())

    // Entonces
    expect(campoTarifa()).toHaveAttribute('aria-invalid', 'true')
    expect(campoTarifa()).toHaveAccessibleDescription(/solo números/)
    expect(peticiones).not.toHaveBeenCalled()
    servidorSimulado.events.removeAllListeners()
  })

  it('CP-TIE-010-02 · si el backend responde TIE-009 lo explica', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(
      http.put('/api/v1/tiendas/me/domicilio', () =>
        HttpResponse.json(
          { title: 'Domicilio inválido', status: 400, codigo: 'TIE-009', errores: [] },
          { status: 400 },
        ),
      ),
    )
    renderizarConProveedores(<FormularioDomicilio domicilio={conDomicilio} />)

    // Cuando
    await usuario.click(botonGuardar())

    // Entonces
    expect(await screen.findByText(/el radio va de 500 m a 10 km/)).toBeInTheDocument()
  })

  it('al apagar el domicilio quita radio y valor y lo guarda así', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioDomicilio domicilio={conDomicilio} />)

    // Cuando
    await usuario.click(screen.getByRole('checkbox', { name: 'Hago domicilios' }))
    await usuario.click(botonGuardar())

    // Entonces
    expect(screen.queryByRole('textbox', { name: 'Valor del domicilio (pesos)' })).not.toBeInTheDocument()
    await vi.waitFor(() => expect(domicilioGuardado()?.haceDomicilio).toBe(false))
  })

  it('al activarlo pide el radio y el valor', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioDomicilio domicilio={sinDomicilio} />)

    // Cuando
    await usuario.click(screen.getByRole('checkbox', { name: 'Hago domicilios' }))
    await usuario.click(botonGuardar())

    // Entonces
    expect(screen.getByRole('combobox', { name: /Hasta qué distancia/ })).toHaveAccessibleDescription(
      /Elige hasta qué distancia/,
    )
    expect(campoTarifa()).toHaveAccessibleDescription(/Escribe el valor del domicilio/)
  })
})

describe('HU-010 · Simular el costo del domicilio (CU-17)', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA)
  })

  it('dice cuánto pagaría un cliente dentro de la cobertura', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<SimuladorDomicilio domicilioGuardado={conDomicilio} />)

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'Calcular' }))

    // Entonces
    expect(await screen.findByText(/El cliente pagaría \$\s4\.000 de domicilio/)).toBeInTheDocument()
  })

  it('TIE-010 · avisa cuando el cliente queda fuera de cobertura', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<SimuladorDomicilio domicilioGuardado={conDomicilio} />)

    // Cuando
    await usuario.selectOptions(screen.getByRole('combobox', { name: 'Distancia del cliente' }), '12000')
    await usuario.click(screen.getByRole('button', { name: 'Calcular' }))

    // Entonces
    expect(await screen.findByText(/queda fuera de tu cobertura/)).toBeInTheDocument()
  })

  it('estado deshabilitado · sin domicilio guardado explica qué hacer', () => {
    renderizarConProveedores(<SimuladorDomicilio domicilioGuardado={sinDomicilio} />)
    expect(screen.getByText(/Activa y guarda el domicilio/)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Calcular' })).not.toBeInTheDocument()
  })
})
