import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA, TIENDAS_SPRINT_2 } from '@/mocks/datos'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { crearSolicitud, MINUTO } from '@/pruebas/solicitudes'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { ConfirmarPedidoPagina } from './ConfirmarPedidoPagina'
import { PedidoPagina } from './PedidoPagina'
import { SolicitudClientePagina } from './SolicitudClientePagina'

const respuestaDe = (solicitudId: string, tiendaId: string) =>
  base.respuestas.find((r) => r.solicitudId === solicitudId && r.tiendaId === tiendaId)!

function abrir(ruta: string) {
  return renderizarConProveedores(
    <Routes>
      <Route path="/solicitudes/:solicitudId" element={<SolicitudClientePagina />} />
      <Route path="/solicitudes/:solicitudId/elegir/:respuestaId" element={<ConfirmarPedidoPagina />} />
      <Route path="/pedidos/:ordenId" element={<PedidoPagina />} />
    </Routes>,
    { ruta },
  )
}

function abrirConfirmar(tiendaId: string = TIENDAS_SPRINT_2.botica, datos = {}) {
  const solicitudId = crearSolicitud(datos)
  const respuesta = respuestaDe(solicitudId, tiendaId)
  return { solicitudId, ...abrir(`/solicitudes/${solicitudId}/elegir/${respuesta.id}`) }
}

const confirmar = () => screen.getByRole('button', { name: /^Confirmar pedido · / })

describe('HU-024 · Elegir una respuesta y crear la orden · CP-ORD-024-01, CP-ORD-024-04', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })

  it('no tiene violaciones de accesibilidad detectables y pone el foco en el título', async () => {
    const { container } = abrirConfirmar()
    await screen.findByRole('radio', { name: 'Efectivo' })
    expect(screen.getByRole('heading', { level: 1, name: 'Confirmar pedido' })).toHaveFocus()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('desde las respuestas, «Elegir» lleva a confirmar el pedido de esa tienda', async () => {
    // Dado
    const usuario = userEvent.setup()
    const solicitudId = crearSolicitud()
    abrir(`/solicitudes/${solicitudId}`)

    // Cuando
    await usuario.click(await screen.findByRole('link', { name: 'Elegir Botica San José' }))

    // Entonces
    expect(await screen.findByText(/^Botica San José · Acetaminofén 500 mg · 1 unidad$/)).toBeInTheDocument()
  })

  it('CP-ORD-024-01 · con domicilio a «Casa» y efectivo crea la orden CONFIRMADO con el total desglosado', async () => {
    // Dado
    const usuario = userEvent.setup()
    const { solicitudId } = abrirConfirmar()
    expect(await screen.findByRole('radio', { name: /Casa/ })).toBeChecked()
    expect(screen.getByRole('radio', { name: /^Domicilio · \$\s3\.000$/ })).toBeChecked()
    expect(
      screen.getByText('Al confirmar, la tienda verá tu nombre, tu teléfono y, si es domicilio, tu dirección.'),
    ).toBeInTheDocument()

    // Cuando
    await usuario.click(screen.getByRole('radio', { name: 'Efectivo' }))
    await usuario.click(confirmar())

    // Entonces
    expect(await screen.findByRole('heading', { level: 1, name: 'Tu pedido' })).toBeInTheDocument()
    expect(await screen.findByText('Confirmado')).toBeInTheDocument()
    const desglose = screen.getByRole('region', { name: 'Detalle del pago' })
    expect(desglose).toHaveTextContent(/Producto \(\$\s2\.500 × 1 unidad\)\$\s2\.500/)
    expect(desglose).toHaveTextContent(/Domicilio\$\s3\.000/)
    expect(desglose).toHaveTextContent(/Total\$\s5\.500/)
    expect(desglose).toHaveTextContent('Pagas con efectivo a la tienda.')
    expect(base.solicitudes.find((s) => s.id === solicitudId)?.estado).toBe('RESUELTA')
  })

  it('al cambiar a recoger quita la dirección y el domicilio del total', async () => {
    // Dado
    const usuario = userEvent.setup()
    abrirConfirmar()
    await screen.findByRole('radio', { name: /Casa/ })

    // Cuando
    await usuario.click(screen.getByRole('radio', { name: 'Recoger en la tienda' }))

    // Entonces
    expect(screen.queryByRole('group', { name: '¿A dónde lo llevamos?' })).not.toBeInTheDocument()
    expect(confirmar()).toHaveAccessibleName(/Confirmar pedido · \$\s2\.500/)
  })

  it('sin medio de pago no envía nada: lo dice en el resumen y en el grupo, y lleva el foco ahí', async () => {
    // Dado
    const usuario = userEvent.setup()
    abrirConfirmar()
    await screen.findByRole('radio', { name: /Casa/ })

    // Cuando
    await usuario.click(confirmar())

    // Entonces
    expect(screen.getByRole('link', { name: 'Elige cómo vas a pagar.' })).toBeInTheDocument()
    expect(screen.getByRole('group', { name: '¿Cómo pagas?' })).toHaveAccessibleDescription(
      expect.stringContaining('Elige cómo vas a pagar.'),
    )
    expect(screen.getByRole('radio', { name: 'Efectivo' })).toHaveFocus()
    expect(base.ordenes).toHaveLength(2)
  })

  it('DEF-007 · CP-TIE-012-01 · solo ofrece los medios de pago que la tienda acepta', async () => {
    // Cuando: Botica San José recibe efectivo y datáfono
    abrirConfirmar()

    // Entonces
    const grupo = await screen.findByRole('group', { name: '¿Cómo pagas?' })
    expect(
      within(grupo)
        .getAllByRole('radio')
        .map((opcion) => opcion.getAttribute('value')),
    ).toEqual(['EFECTIVO', 'DATAFONO'])
    expect(within(grupo).queryByRole('radio', { name: 'Transferencia' })).not.toBeInTheDocument()
  })

  it('CP-ORD-024-04 · si la tienda dejó de aceptar el medio mientras elegía, lo dice en ese grupo y no crea la orden', async () => {
    // Dado: Botica San José recibía efectivo y datáfono, y quitó el datáfono después de que cargó la pantalla
    const usuario = userEvent.setup()
    abrirConfirmar()
    await screen.findByRole('radio', { name: 'Datáfono' })
    base.tiendas.find((t) => t.id === TIENDAS_SPRINT_2.botica)!.mediosPago = ['EFECTIVO']

    // Cuando
    await usuario.click(screen.getByRole('radio', { name: 'Datáfono' }))
    await usuario.click(confirmar())

    // Entonces
    const grupo = screen.getByRole('group', { name: '¿Cómo pagas?' })
    expect(await within(grupo).findByText('La tienda no recibe ese medio de pago. Elige otro.')).toBeInTheDocument()
    expect(screen.getByRole('radio', { name: 'Datáfono' })).toHaveFocus()
    expect(base.ordenes).toHaveLength(2)
  })

  it('si la tienda no hace domicilio, solo ofrece recoger', async () => {
    // Cuando: Farmacia Popular no hace domicilio
    abrirConfirmar(TIENDAS_SPRINT_2.popular)

    // Entonces
    expect(await screen.findByText('Esta tienda no hace domicilio hasta tu ubicación.')).toBeInTheDocument()
    expect(screen.getByRole('radio', { name: 'Recoger en la tienda' })).toBeChecked()
    expect(screen.queryByRole('radio', { name: /^Domicilio/ })).not.toBeInTheDocument()
  })

  it('si la solicitud venció, dice que ya no se puede elegir en vez de mostrar el formulario', async () => {
    // Cuando
    abrirConfirmar(TIENDAS_SPRINT_2.botica, { fechaVencimiento: new Date(Date.now() - MINUTO).toISOString() })

    // Entonces
    expect(await screen.findByText('Esta respuesta ya no se puede elegir.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Confirmar pedido/ })).not.toBeInTheDocument()
  })
})
