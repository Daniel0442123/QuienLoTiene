import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA, TIENDAS_SPRINT_2 } from '@/mocks/datos'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { ResponderSolicitudPagina } from './ResponderSolicitudPagina'

const PARACETAMOL = 'a5b6c7d8-0001-4e2f-9a3b-1c2d3e4f5a01'
const respuestaDeLa5 = () =>
  base.respuestas.find((r) => r.tiendaId === TIENDAS_SPRINT_2.la5 && r.solicitudId === PARACETAMOL)

function abrir(consulta = '') {
  return renderizarConProveedores(
    <Routes>
      <Route path="/tienda/solicitudes/mis-respuestas" element={<h1>Mis respuestas</h1>} />
      <Route path="/tienda/solicitudes/:solicitudId" element={<ResponderSolicitudPagina />} />
    </Routes>,
    { ruta: `/tienda/solicitudes/${PARACETAMOL}${consulta}` },
  )
}

const enviar = () => screen.getByRole('button', { name: 'Enviar respuesta' })

describe('HU-019 · Responder una solicitud', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA)
  })

  it('no tiene violaciones de accesibilidad detectables y pone el foco en el producto', async () => {
    const { container } = abrir()
    expect(await screen.findByRole('heading', { level: 1, name: 'Paracetamol 500 mg' })).toHaveFocus()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-SOL-019-01 · «Sí lo tengo» en un toque más: precio y unidades ya vienen llenos (SC-011)', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    abrir()

    // Entonces: el último precio de la tienda para este producto y las unidades pedidas
    expect(await screen.findByLabelText('Precio por unidad')).toHaveValue('800')
    expect(screen.getByLabelText('Unidades que tienes')).toHaveValue('2')
    expect(screen.getByText('Es el último precio que diste para este producto.')).toBeInTheDocument()
    expect(screen.getByText(/^El cliente verá: \$\s800 por 2 unidades$/)).toBeInTheDocument()

    // Cuando
    await usuario.click(enviar())

    // Entonces
    expect(await screen.findByRole('heading', { name: 'Mis respuestas' })).toBeInTheDocument()
    expect(respuestaDeLa5()).toMatchObject({ tipo: 'SI', precioEnPesos: 800, cantidadDisponible: 2 })
  })

  it('cambia las unidades con los botones sin pasar de lo que pidió el cliente', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    abrir()
    const agregar = await screen.findByRole('button', { name: 'Agregar una unidad' })
    expect(agregar).toBeDisabled()

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'Quitar una unidad' }))

    // Entonces
    expect(screen.getByLabelText('Unidades que tienes')).toHaveValue('1')
    expect(screen.getByRole('button', { name: 'Quitar una unidad' })).toBeDisabled()
    expect(agregar).toBeEnabled()
  })

  it('CP-SOL-019-02 · «Tengo algo parecido» sin descripción no se envía: lo dice arriba y en el campo', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    abrir('?tipo=PARECIDO')
    const descripcion = await screen.findByLabelText('¿Qué ofreces en su lugar?')

    // Cuando
    await usuario.click(enviar())

    // Entonces
    expect(screen.getByRole('heading', { name: 'Revisa esto antes de enviar:' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Describe qué producto ofreces.' })).toBeInTheDocument()
    expect(descripcion).toHaveAccessibleDescription(expect.stringContaining('Describe qué producto ofreces.'))
    expect(descripcion).toHaveAttribute('aria-invalid', 'true')
    expect(descripcion).toHaveFocus()
    expect(respuestaDeLa5()).toBeUndefined()

    // Y al describirla se envía como alternativa
    await usuario.type(descripcion, 'Acetaminofén 500 mg genérico')
    await usuario.click(enviar())
    await vi.waitFor(() =>
      expect(respuestaDeLa5()).toMatchObject({
        tipo: 'PARECIDO',
        descripcionAlternativa: 'Acetaminofén 500 mg genérico',
      }),
    )
  })

  it('CP-SOL-019-04 · si ya respondió (por ejemplo en otro celular) no muestra el formulario sino su respuesta', async () => {
    // Dado: la tienda respondió desde otro dispositivo
    base.respuestas.push({
      id: 'b6c7d8e9-0099-4f3a-8b4c-2d3e4f5a6b99',
      solicitudId: PARACETAMOL,
      tiendaId: TIENDAS_SPRINT_2.la5,
      tipo: 'SI',
      precioEnPesos: 8000,
      cantidadDisponible: 1,
      descripcionAlternativa: null,
      tiempoEstimadoEnMinutos: null,
      ofreceDomicilio: true,
      costoDomicilioEnPesos: 4000,
      fechaRespuesta: new Date().toISOString(),
    })

    // Cuando
    abrir()

    // Entonces
    expect(
      await screen.findByText(/A las .* respondiste esta solicitud con «Sí lo tengo», \$\s8\.000 por 1 unidad\./),
    ).toBeInTheDocument()
    expect(screen.getByText('Esperando a que el cliente elija.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Enviar respuesta' })).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Volver a solicitudes nuevas' })).toBeInTheDocument()
  })

  it('si la solicitud venció mientras respondía, dice que ya se cerró', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    abrir()
    await screen.findByLabelText('Precio por unidad')
    const solicitud = base.solicitudes.find((s) => s.id === PARACETAMOL)!
    solicitud.fechaVencimiento = new Date(Date.now() - 1000).toISOString()

    // Cuando
    await usuario.click(enviar())

    // Entonces
    expect(await screen.findByText('Esta solicitud ya no está disponible.')).toBeInTheDocument()
  })
})
