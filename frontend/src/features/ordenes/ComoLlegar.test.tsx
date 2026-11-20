import { screen } from '@testing-library/react'
import { beforeEach, describe, expect, it } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA, TIENDAS_SPRINT_2 } from '@/mocks/datos'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { abrirPedidoCliente, ORDEN_DE_LA_CLIENTA, ORDEN_DE_LA_VECINA, ponerOrdenEn } from '@/pruebas/ordenes'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { enlaceComoLlegar } from './presentacion'

const enlace = () => screen.findByRole('link', { name: /Cómo llegar/ })

describe('HU-026 · Cómo llegar · CP-ORD-026-01', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })

  it('CP-ORD-026-01 · en una recogida Confirmada abre la app de mapas con la ubicación de la tienda', async () => {
    // Dado: la clienta va a recoger en Droguería La 5
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'CONFIRMADO')
    const la5 = base.tiendas.find((t) => t.id === TIENDAS_SPRINT_2.la5)!

    // Cuando
    const { container } = abrirPedidoCliente(ORDEN_DE_LA_CLIENTA)

    // Entonces: la ruta va a la tienda, en otra pestaña y sin datos del cliente en la URL
    const comoLlegar = await enlace()
    // Dice que sale de la app (WCAG 3.2.5)
    expect(comoLlegar).toHaveTextContent('Cómo llegar (se abre la app de mapas)')
    expect(comoLlegar).toHaveAttribute(
      'href',
      `https://www.google.com/maps/dir/?api=1&destination=${la5.latitud},${la5.longitud}`,
    )
    expect(comoLlegar).toHaveAttribute('target', '_blank')
    expect(comoLlegar).toHaveAttribute('rel', 'noopener noreferrer')
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('no se ofrece en un pedido a domicilio', async () => {
    // Dado
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.vecina, FRASE_DE_PRUEBA)
    ponerOrdenEn(ORDEN_DE_LA_VECINA, 'CONFIRMADO')

    // Cuando
    abrirPedidoCliente(ORDEN_DE_LA_VECINA)

    // Entonces
    expect(await screen.findByRole('button', { name: 'Cancelar pedido' })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /Cómo llegar/ })).not.toBeInTheDocument()
  })

  it('no se ofrece con el pedido cerrado', async () => {
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'CANCELADO')
    abrirPedidoCliente(ORDEN_DE_LA_CLIENTA)
    expect(await screen.findByText(/cancelaste el pedido|canceló el pedido/)).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /Cómo llegar/ })).not.toBeInTheDocument()
  })

  it('el enlace solo lleva las coordenadas de la tienda', () => {
    expect(enlaceComoLlegar(2.4448, -76.6147)).toBe(
      'https://www.google.com/maps/dir/?api=1&destination=2.4448,-76.6147',
    )
  })
})
