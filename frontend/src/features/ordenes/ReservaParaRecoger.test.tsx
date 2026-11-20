import { act, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import {
  abrirPedidoCliente,
  abrirPedidoTienda,
  ORDEN_DE_LA_CLIENTA,
  ordenEnBase,
  ponerOrdenEn,
} from '@/pruebas/ordenes'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'

const MINUTO = 60_000

async function entrarComo(correo: string) {
  limpiarSesion()
  await iniciarSesion(correo, FRASE_DE_PRUEBA)
}

describe('HU-025 · Reserva para recoger · CP-ORD-025-01, CP-ORD-025-02', () => {
  beforeEach(() => entrarComo(CORREOS_DE_PRUEBA.cliente))
  afterEach(() => vi.useRealTimers())

  it('SC-019 · la reserva empieza cuando la tienda marca «Listo para recoger» y dura 30 minutos', async () => {
    // Dado: La 5 está preparando el pedido para recoger de la clienta
    const usuario = userEvent.setup({ delay: null })
    await entrarComo(CORREOS_DE_PRUEBA.tienda)
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'PREPARANDO')
    abrirPedidoTienda(ORDEN_DE_LA_CLIENTA)

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Listo para recoger' }))

    // Entonces: vence 30 minutos después del cambio, no desde la orden ni la respuesta
    expect(await screen.findByText(/^El cliente tiene hasta las .* para recogerlo$/)).toBeInTheDocument()
    const orden = ordenEnBase(ORDEN_DE_LA_CLIENTA)
    const listo = orden.historial.find((cambio) => cambio.estado === 'LISTO_PARA_RECOGER')!
    expect(Date.parse(orden.reservaVence!) - Date.parse(listo.fecha)).toBe(30 * MINUTO)
  })

  it('el cliente ve hasta qué hora se lo guardan y cuántos minutos quedan', async () => {
    // Dado
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'LISTO_PARA_RECOGER', 24)

    // Cuando
    const { container } = abrirPedidoCliente(ORDEN_DE_LA_CLIENTA)

    // Entonces
    expect(await screen.findByText(/^Te lo guardan hasta las .*$/)).toBeInTheDocument()
    expect(screen.getByText('Quedan 24 minutos')).toBeInTheDocument()
    expect(screen.getByText('Si no llegas, la reserva vence y la tienda puede venderlo.')).toBeInTheDocument()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-ORD-025-01 · si pasan los 30 minutos sin recoger, el pedido queda Vencido para los dos', async () => {
    // Dado: la reserva venció hace un minuto
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'LISTO_PARA_RECOGER', -1)

    // Cuando
    const { unmount, container } = abrirPedidoCliente(ORDEN_DE_LA_CLIENTA)

    // Entonces
    expect(await screen.findByText('La reserva venció')).toBeInTheDocument()
    expect(screen.getByText(/^A las .* venció la reserva\. La tienda ya puede vender el producto/)).toBeInTheDocument()
    expect(screen.getByText('Vencido')).toBeInTheDocument()
    expect(screen.queryByText(/^Te lo guardan hasta las/)).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Cancelar pedido' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /Cómo llegar/ })).not.toBeInTheDocument()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])

    // Y la tienda
    unmount()
    await entrarComo(CORREOS_DE_PRUEBA.tienda)
    abrirPedidoTienda(ORDEN_DE_LA_CLIENTA)
    expect(
      await screen.findByText(/^A las .* venció la reserva: el cliente no llegó\. Ya puedes vender el producto\.$/),
    ).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Marcar como entregado' })).not.toBeInTheDocument()
  })

  it('CP-ORD-025-01 · con la pantalla abierta, al llegar la hora pasa a Vencido sin recargar', async () => {
    // Dado: quedan 2 minutos
    vi.useFakeTimers({ shouldAdvanceTime: true })
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'LISTO_PARA_RECOGER', 2)
    abrirPedidoCliente(ORDEN_DE_LA_CLIENTA)
    expect(await screen.findByText('Quedan 2 minutos')).toBeInTheDocument()

    // Cuando: pasan 3 minutos
    await act(() => vi.advanceTimersByTimeAsync(3 * MINUTO))

    // Entonces
    expect(await screen.findByText('La reserva venció')).toBeInTheDocument()
    expect(ordenEnBase(ORDEN_DE_LA_CLIENTA).estado).toBe('VENCIDA')
  })

  it('avisa una sola vez por el lector de pantalla cuando quedan 5 minutos', async () => {
    // Dado: quedan 6 minutos
    vi.useFakeTimers({ shouldAdvanceTime: true })
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'LISTO_PARA_RECOGER', 6)
    abrirPedidoCliente(ORDEN_DE_LA_CLIENTA)
    expect(await screen.findByText('Quedan 6 minutos')).toBeInTheDocument()
    expect(screen.queryByText('Quedan 5 minutos de tu reserva para recoger el pedido.')).not.toBeInTheDocument()

    // Cuando: pasan un minuto y medio
    await act(() => vi.advanceTimersByTimeAsync(1.5 * MINUTO))

    // Entonces
    expect(await screen.findByText('Quedan 5 minutos de tu reserva para recoger el pedido.')).toBeInTheDocument()
  })

  it('CP-ORD-025-02 · si recoge a tiempo, la tienda lo entrega y el pedido queda Recibido', async () => {
    // Dado: la reserva sigue vigente y la tienda lo entrega
    const usuario = userEvent.setup({ delay: null })
    await entrarComo(CORREOS_DE_PRUEBA.tienda)
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'LISTO_PARA_RECOGER', 20)
    const { unmount } = abrirPedidoTienda(ORDEN_DE_LA_CLIENTA)
    await usuario.click(await screen.findByRole('button', { name: 'Marcar como entregado' }))
    expect(await screen.findByText('Esperando a que el cliente confirme que lo recibió.')).toBeInTheDocument()
    unmount()

    // Cuando: la clienta confirma
    await entrarComo(CORREOS_DE_PRUEBA.cliente)
    abrirPedidoCliente(ORDEN_DE_LA_CLIENTA)
    await usuario.click(await screen.findByRole('button', { name: 'Recibí mi pedido' }))

    // Entonces
    expect(await screen.findByText('Recibido')).toBeInTheDocument()
    expect(ordenEnBase(ORDEN_DE_LA_CLIENTA).estado).toBe('RESUELTA')
  })
})
