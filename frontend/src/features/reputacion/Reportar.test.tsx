import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CODIGO_TOTP_DE_PRUEBA, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { PedidoPagina } from '@/pages/PedidoPagina'
import { PedidoTiendaPagina } from '@/pages/PedidoTiendaPagina'
import { ReportarProblemaClientePagina, ReportarProblemaTiendaPagina } from '@/pages/ReportarProblemaPagina'
import { ReportesAdminPagina } from '@/pages/ReportesAdminPagina'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { ORDEN_DE_LA_CLIENTA, ORDEN_DE_LA_VECINA, ponerOrdenEn } from '@/pruebas/ordenes'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { clienteApi } from '@/services/clienteApi'
import { esErrorApi } from '@/services/errores'
import { iniciarSesion, limpiarSesion, verificarSegundoFactor } from '@/services/sesion'

function abrir(ruta: string) {
  return renderizarConProveedores(
    <Routes>
      <Route path="/pedidos/:ordenId" element={<PedidoPagina />} />
      <Route path="/pedidos/:ordenId/reportar" element={<ReportarProblemaClientePagina />} />
      <Route path="/tienda/pedidos/:ordenId" element={<PedidoTiendaPagina />} />
      <Route path="/tienda/pedidos/:ordenId/reportar" element={<ReportarProblemaTiendaPagina />} />
      <Route path="/admin/reportes" element={<ReportesAdminPagina />} />
    </Routes>,
    { ruta },
  )
}

async function entrarComo(correo: string) {
  limpiarSesion()
  await iniciarSesion(correo, FRASE_DE_PRUEBA)
}

const enviar = () => screen.getByRole('button', { name: 'Enviar reporte' })
const reportesNuevos = () => base.reportes.filter((r) => !r.id.startsWith('a7b8c9d0-'))

describe('HU-035 · Reportar un problema · CP-REP-035-01, CP-REP-035-02', () => {
  beforeEach(() => entrarComo(CORREOS_DE_PRUEBA.vecina))

  it('no tiene violaciones de accesibilidad, vacío y con errores', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    const { container } = abrir(`/pedidos/${ORDEN_DE_LA_VECINA}/reportar`)
    await screen.findByText(/^Pedido E1F2A3B4 a Droguería La 5$/)
    expect(await violacionesDeAccesibilidad(container)).toEqual([])

    // Cuando
    await usuario.click(enviar())

    // Entonces: el resumen y el foco en el primer error
    expect(await screen.findByRole('heading', { name: 'Revisa esto antes de enviar:' })).toBeInTheDocument()
    expect(screen.getAllByText('Elige qué pasó.').length).toBeGreaterThan(0)
    await vi.waitFor(() => expect(screen.getByRole('radio', { name: 'Demora en la entrega' })).toHaveFocus())
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-REP-035-01 · con el pedido en camino, reporta «Demora en la entrega» y queda Abierto para el administrador', async () => {
    // Dado: el pedido de la vecina va en camino
    const usuario = userEvent.setup({ delay: null })
    ponerOrdenEn(ORDEN_DE_LA_VECINA, 'EN_CAMINO')
    const { unmount } = abrir(`/pedidos/${ORDEN_DE_LA_VECINA}`)

    // Cuando: entra desde el pedido, elige el motivo y cuenta qué pasa
    await usuario.click(await screen.findByRole('link', { name: 'Reportar un problema' }))
    await usuario.click(await screen.findByRole('radio', { name: 'Demora en la entrega' }))
    await usuario.type(screen.getByRole('textbox', { name: 'Cuéntanos más (opcional)' }), 'Ya pasó una hora.')
    await usuario.click(enviar())

    // Entonces: vuelve al pedido y lo anuncia
    expect(
      await screen.findByText('Enviamos tu reporte. El equipo de ¿Quién lo tiene? lo revisará.'),
    ).toBeInTheDocument()
    expect(await screen.findByRole('heading', { level: 1, name: 'Tu pedido' })).toBeInTheDocument()
    expect(reportesNuevos()).toEqual([
      expect.objectContaining({
        ordenId: ORDEN_DE_LA_VECINA,
        reportadoPor: 'CLIENTE',
        motivo: 'DEMORA_ENTREGA',
        descripcion: 'Ya pasó una hora.',
        estado: 'ABIERTO',
      }),
    ])

    // Y el administrador lo ve entre los abiertos
    unmount()
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.administrador, FRASE_DE_PRUEBA)
    await verificarSegundoFactor(CODIGO_TOTP_DE_PRUEBA)
    abrir('/admin/reportes')
    expect(await screen.findByText('«Ya pasó una hora.»')).toBeInTheDocument()
  })

  it('con «Otro problema» la descripción es obligatoria y la etiqueta lo dice', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    abrir(`/pedidos/${ORDEN_DE_LA_VECINA}/reportar`)

    // Cuando
    await usuario.click(await screen.findByRole('radio', { name: 'Otro problema' }))
    await usuario.click(enviar())

    // Entonces
    const campo = screen.getByRole('textbox', { name: 'Cuéntanos qué pasó' })
    expect(campo).toHaveAttribute('aria-required', 'true')
    expect(screen.getAllByText('Con «Otro problema», cuéntanos qué pasó.').length).toBeGreaterThan(0)
    await vi.waitFor(() => expect(campo).toHaveFocus())
    expect(reportesNuevos()).toHaveLength(0)

    // Cuando: la escribe
    await usuario.type(campo, 'El domiciliario fue grosero.')
    await usuario.click(enviar())

    // Entonces
    await vi.waitFor(() => expect(reportesNuevos()).toHaveLength(1))
    expect(reportesNuevos()[0]).toMatchObject({ motivo: 'OTRO', descripcion: 'El domiciliario fue grosero.' })
  })

  it('la tienda también reporta desde su pedido', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    await entrarComo(CORREOS_DE_PRUEBA.tienda)
    abrir(`/tienda/pedidos/${ORDEN_DE_LA_VECINA}`)

    // Cuando
    await usuario.click(await screen.findByRole('link', { name: 'Reportar un problema' }))
    expect(await screen.findByText(/^Pedido E1F2A3B4$/)).toBeInTheDocument()
    await usuario.click(screen.getByRole('radio', { name: 'Me cobraron diferente' }))
    await usuario.click(enviar())

    // Entonces
    expect(
      await screen.findByText('Enviamos tu reporte. El equipo de ¿Quién lo tiene? lo revisará.'),
    ).toBeInTheDocument()
    expect(reportesNuevos()[0]).toMatchObject({ reportadoPor: 'TIENDA', motivo: 'COBRO_DIFERENTE', descripcion: null })
  })

  it('CP-REP-035-02 · no puede reportar un pedido en el que no participa (ORD-001)', async () => {
    // Dado: la clienta de prueba no participa en el pedido de la vecina
    await entrarComo(CORREOS_DE_PRUEBA.cliente)

    // Cuando: por la API
    const fallo = await clienteApi
      .POST('/api/v1/ordenes/{ordenId}/reportes', {
        params: { path: { ordenId: ORDEN_DE_LA_VECINA } },
        body: { motivo: 'DEMORA_ENTREGA' },
      })
      .catch((error: unknown) => error)

    // Entonces: se rechaza sin decir que el pedido existe
    expect(esErrorApi(fallo) && fallo.codigo).toBe('ORD-001')
    expect(reportesNuevos()).toHaveLength(0)

    // Y la pantalla tampoco lo muestra
    abrir(`/pedidos/${ORDEN_DE_LA_VECINA}/reportar`)
    expect(await screen.findByText('No encontramos ese pedido.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Enviar reporte' })).not.toBeInTheDocument()
  })

  it('el pedido de la clienta ofrece reportar en cualquier estado', async () => {
    await entrarComo(CORREOS_DE_PRUEBA.cliente)
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'RESUELTA')
    abrir(`/pedidos/${ORDEN_DE_LA_CLIENTA}`)
    const enlace = await screen.findByRole('link', { name: 'Reportar un problema' })
    expect(enlace).toHaveAttribute('href', `/pedidos/${ORDEN_DE_LA_CLIENTA}/reportar`)
    expect(within(document.body).queryByRole('button', { name: 'Cancelar pedido' })).not.toBeInTheDocument()
  })
})
