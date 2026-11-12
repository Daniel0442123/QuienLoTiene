import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { FormularioMediosPago } from './FormularioMediosPago'

const mediosGuardados = () => base.tiendas.find((t) => t.estado === 'APROBADA')?.mediosPago
const casilla = (nombre: RegExp) => screen.getByRole('checkbox', { name: nombre })
const botonGuardar = () => screen.getByRole('button', { name: 'Guardar cambios' })

describe('HU-012 · Medios de pago de la tienda', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA)
  })

  it('no tiene violaciones de accesibilidad detectables', async () => {
    const { container } = renderizarConProveedores(<FormularioMediosPago mediosPago={['EFECTIVO']} />)
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-TIE-012-01 · muestra marcados solo los medios activos', () => {
    renderizarConProveedores(<FormularioMediosPago mediosPago={['EFECTIVO', 'TRANSFERENCIA']} />)
    expect(casilla(/Efectivo/)).toBeChecked()
    expect(casilla(/Transferencia/)).toBeChecked()
    expect(casilla(/Datáfono/)).not.toBeChecked()
  })

  it('CP-TIE-012-02 · al desmarcar el datáfono deja de quedar activo', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioMediosPago mediosPago={['EFECTIVO', 'DATAFONO']} />)

    // Cuando
    await usuario.click(casilla(/Datáfono/))
    await usuario.click(botonGuardar())

    // Entonces
    await vi.waitFor(() => expect(mediosGuardados()).toEqual(['EFECTIVO']))
    expect(await screen.findByText('Medios de pago actualizados.', { selector: 'p.text-exito' })).toBeInTheDocument()
  })

  it('sin ningún medio no envía nada, lo explica y lleva el foco a la primera casilla', async () => {
    // Dado
    const usuario = userEvent.setup()
    const peticiones = vi.fn()
    servidorSimulado.events.on('request:start', ({ request }) => {
      if (request.method === 'PUT') peticiones()
    })
    renderizarConProveedores(<FormularioMediosPago mediosPago={['EFECTIVO']} />)

    // Cuando
    await usuario.click(casilla(/Efectivo/))
    await usuario.click(botonGuardar())

    // Entonces
    expect(screen.getByRole('group', { name: 'Medios de pago que aceptas' })).toHaveAccessibleDescription(
      /Elige al menos un medio de pago/,
    )
    expect(casilla(/Efectivo/)).toHaveFocus()
    expect(peticiones).not.toHaveBeenCalled()
    servidorSimulado.events.removeAllListeners()
  })

  it('TIE-011 · si el backend lo rechaza muestra el mensaje', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(
      http.put('/api/v1/tiendas/me/medios-pago', () =>
        HttpResponse.json({ title: 'Medios inválidos', status: 400, codigo: 'TIE-011', errores: [] }, { status: 400 }),
      ),
    )
    renderizarConProveedores(<FormularioMediosPago mediosPago={['EFECTIVO']} />)

    // Cuando
    await usuario.click(botonGuardar())

    // Entonces
    expect(await screen.findByText(/efectivo, transferencia o datáfono/)).toBeInTheDocument()
  })
})
