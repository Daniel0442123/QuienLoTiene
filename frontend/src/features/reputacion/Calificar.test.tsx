import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA, TIENDAS_SPRINT_2 } from '@/mocks/datos'
import { CalificarPedidoPagina } from '@/pages/CalificarPedidoPagina'
import { DetalleTiendaPagina } from '@/pages/DetalleTiendaPagina'
import { PedidoPagina } from '@/pages/PedidoPagina'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { abrirPedidoTienda, ORDEN_DE_LA_CLIENTA, ponerOrdenEn } from '@/pruebas/ordenes'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'

vi.mock('@/components/MapaUbicacion', () => ({ default: () => null }))

function abrir(ruta: string) {
  return renderizarConProveedores(
    <Routes>
      <Route path="/pedidos/:ordenId" element={<PedidoPagina />} />
      <Route path="/pedidos/:ordenId/calificar" element={<CalificarPedidoPagina />} />
      <Route path="/tiendas/:tiendaId" element={<DetalleTiendaPagina />} />
    </Routes>,
    { ruta },
  )
}

const resenaDeLaOrden = () => base.resenas.find((r) => r.ordenId === ORDEN_DE_LA_CLIENTA)
const publicar = () => screen.getByRole('button', { name: 'Publicar calificación' })

async function entrarComo(correo: string) {
  limpiarSesion()
  await iniciarSesion(correo, FRASE_DE_PRUEBA)
}

describe('HU-034 · Calificar la tienda · CP-REP-034-01, CP-REP-034-02, CP-REP-034-03', () => {
  beforeEach(() => entrarComo(CORREOS_DE_PRUEBA.cliente))

  it('no tiene violaciones de accesibilidad, vacío y con errores', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'RESUELTA')
    const { container } = abrir(`/pedidos/${ORDEN_DE_LA_CLIENTA}/calificar`)
    await screen.findByRole('heading', { level: 1, name: '¿Cómo te fue con Droguería La 5?' })
    expect(await violacionesDeAccesibilidad(container)).toEqual([])

    // Cuando
    await usuario.click(publicar())

    // Entonces
    expect(await screen.findByRole('heading', { name: 'Revisa esto antes de publicar:' })).toBeInTheDocument()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-REP-034-01 · califica con 4 estrellas, la reseña se publica y el promedio de la tienda se recalcula', async () => {
    // Dado: la clienta recibió su pedido de Droguería La 5 (promedio 4,7 con 3 reseñas)
    const usuario = userEvent.setup({ delay: null })
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'RESUELTA')
    const { unmount } = abrir(`/pedidos/${ORDEN_DE_LA_CLIENTA}`)

    // Cuando: entra desde el pedido, elige 4 estrellas y escribe un comentario
    await usuario.click(await screen.findByRole('link', { name: 'Calificar a Droguería La 5' }))
    const titulo = await screen.findByRole('heading', { level: 1, name: '¿Cómo te fue con Droguería La 5?' })
    await vi.waitFor(() => expect(titulo).toHaveFocus())
    await usuario.click(screen.getByRole('radio', { name: '4 estrellas' }))
    expect(screen.getByText('Calificación: 4 de 5 estrellas')).toBeInTheDocument()
    await usuario.type(screen.getByRole('textbox', { name: 'Comentario (opcional)' }), 'Llegó rápido y bien empacado.')
    await usuario.click(publicar())

    // Entonces: vuelve al pedido, que muestra su calificación
    expect(await screen.findByRole('heading', { name: 'Tu calificación' })).toBeInTheDocument()
    expect(screen.getAllByText('4 de 5 estrellas').length).toBeGreaterThan(0)
    expect(screen.getByText('«Llegó rápido y bien empacado.»')).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Calificar a Droguería La 5' })).not.toBeInTheDocument()
    expect(resenaDeLaOrden()).toMatchObject({ estrellas: 4, comentario: 'Llegó rápido y bien empacado.' })

    // Y el promedio de la tienda: (5 + 4 + 5 + 4) / 4 = 4,5 con 4 reseñas
    unmount()
    abrir(`/tiendas/${TIENDAS_SPRINT_2.la5}`)
    expect(await screen.findByText('4,5 de 5 · 4 reseñas')).toBeInTheDocument()
  })

  it('sin comentario no lo envía y la reseña queda sin comentario', async () => {
    const usuario = userEvent.setup({ delay: null })
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'RESUELTA')
    abrir(`/pedidos/${ORDEN_DE_LA_CLIENTA}/calificar`)
    await usuario.click(await screen.findByRole('radio', { name: '5 estrellas' }))
    await usuario.click(publicar())
    expect(await screen.findByRole('heading', { name: 'Tu calificación' })).toBeInTheDocument()
    expect(resenaDeLaOrden()).toMatchObject({ estrellas: 5, comentario: null })
  })

  it('pide las estrellas y un comentario de máximo 500 caracteres, con el resumen al primer intento', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'RESUELTA')
    abrir(`/pedidos/${ORDEN_DE_LA_CLIENTA}/calificar`)
    const comentario = await screen.findByRole('textbox', { name: 'Comentario (opcional)' })

    // Cuando
    await usuario.click(comentario)
    await usuario.paste('a'.repeat(501))
    await usuario.click(publicar())

    // Entonces: los dos errores a la vez, en el resumen, y el foco en las estrellas
    const resumen = await screen.findByRole('heading', { name: 'Revisa esto antes de publicar:' })
    const lista = within(resumen.parentElement!)
    expect(lista.getByRole('link', { name: 'Elige de 1 a 5 estrellas.' })).toBeInTheDocument()
    expect(lista.getByRole('link', { name: 'El comentario puede tener máximo 500 caracteres.' })).toBeInTheDocument()
    await vi.waitFor(() => expect(screen.getByRole('radio', { name: '1 estrella' })).toHaveFocus())
    expect(resenaDeLaOrden()).toBeUndefined()
  })

  it('CP-REP-034-02 · con el pedido sin resolver no ofrece calificar', async () => {
    // Dado: la tienda lo entregó pero la clienta no ha confirmado
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'ENTREGADO')

    // Cuando
    const { unmount } = abrir(`/pedidos/${ORDEN_DE_LA_CLIENTA}`)

    // Entonces
    expect(await screen.findByRole('button', { name: 'Recibí mi pedido' })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /Calificar a/ })).not.toBeInTheDocument()

    // Y si abre la dirección directamente, se le explica
    unmount()
    abrir(`/pedidos/${ORDEN_DE_LA_CLIENTA}/calificar`)
    expect(await screen.findByText('Puedes calificar cuando confirmes que recibiste el pedido.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Publicar calificación' })).not.toBeInTheDocument()
  })

  it('CP-REP-034-02 · si la API la rechaza (REP-001), lo dice y no publica nada', async () => {
    // Dado: el formulario está abierto, pero la orden ya no está resuelta (por ejemplo, la revisa soporte)
    const usuario = userEvent.setup({ delay: null })
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'RESUELTA')
    abrir(`/pedidos/${ORDEN_DE_LA_CLIENTA}/calificar`)
    await usuario.click(await screen.findByRole('radio', { name: '3 estrellas' }))
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'ENTREGADO')

    // Cuando
    await usuario.click(publicar())

    // Entonces
    expect(
      await screen.findByText('Solo puedes calificar un pedido tuyo después de confirmar que lo recibiste.'),
    ).toBeInTheDocument()
    expect(resenaDeLaOrden()).toBeUndefined()
  })

  it('CP-REP-034-03 · un pedido ya calificado muestra la calificación y no deja calificar otra vez', async () => {
    // Dado: ya lo calificó
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'RESUELTA')
    base.resenas.push({
      id: 'f1a2b3c4-0099-4d5e-8f9a-0b1c2d3e4f99',
      ordenId: ORDEN_DE_LA_CLIENTA,
      tiendaId: TIENDAS_SPRINT_2.la5,
      clienteId: base.usuarios.find((u) => u.correo === CORREOS_DE_PRUEBA.cliente)!.id,
      estrellas: 2,
      comentario: 'Tardaron mucho',
      fecha: new Date().toISOString(),
    })

    // Cuando
    abrir(`/pedidos/${ORDEN_DE_LA_CLIENTA}/calificar`)

    // Entonces
    expect(await screen.findByRole('heading', { level: 1, name: 'Ya calificaste este pedido' })).toBeInTheDocument()
    expect(screen.getByText('«Tardaron mucho»')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Publicar calificación' })).not.toBeInTheDocument()
  })

  it('CP-REP-034-03 · si calificó en otra pestaña mientras tanto, REP-002 lo dice y muestra la que quedó', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'RESUELTA')
    abrir(`/pedidos/${ORDEN_DE_LA_CLIENTA}/calificar`)
    await usuario.click(await screen.findByRole('radio', { name: '5 estrellas' }))
    base.resenas.push({
      id: 'f1a2b3c4-0098-4d5e-8f9a-0b1c2d3e4f98',
      ordenId: ORDEN_DE_LA_CLIENTA,
      tiendaId: TIENDAS_SPRINT_2.la5,
      clienteId: base.usuarios.find((u) => u.correo === CORREOS_DE_PRUEBA.cliente)!.id,
      estrellas: 4,
      comentario: null,
      fecha: new Date().toISOString(),
    })

    // Cuando
    await usuario.click(publicar())

    // Entonces: queda una sola, la primera
    expect(await screen.findByRole('heading', { level: 1, name: 'Ya calificaste este pedido' })).toBeInTheDocument()
    expect(base.resenas.filter((r) => r.ordenId === ORDEN_DE_LA_CLIENTA)).toHaveLength(1)
  })

  it('la tienda ve la calificación que le dejó el cliente', async () => {
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'RESUELTA')
    base.resenas.push({
      id: 'f1a2b3c4-0097-4d5e-8f9a-0b1c2d3e4f97',
      ordenId: ORDEN_DE_LA_CLIENTA,
      tiendaId: TIENDAS_SPRINT_2.la5,
      clienteId: base.usuarios.find((u) => u.correo === CORREOS_DE_PRUEBA.cliente)!.id,
      estrellas: 5,
      comentario: 'Excelente',
      fecha: new Date().toISOString(),
    })
    await entrarComo(CORREOS_DE_PRUEBA.tienda)
    abrirPedidoTienda(ORDEN_DE_LA_CLIENTA)
    expect(await screen.findByRole('heading', { name: 'Calificación del cliente' })).toBeInTheDocument()
    expect(screen.getByText('«Excelente»')).toBeInTheDocument()
  })
})
