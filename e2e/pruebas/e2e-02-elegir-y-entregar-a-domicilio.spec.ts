import { expect, test } from '@playwright/test'
import { entregarPedidoADomicilio } from './apoyo/flujos'

/**
 * E2E-02 · Elegir y entregar a domicilio (HU-020, HU-024, HU-027, HU-028).
 * Cliente elige una respuesta con domicilio, confirma dirección y medio de pago; la tienda lleva la orden de Confirmado
 * a Entregado y el cliente confirma la recepción.
 */
test('E2E-02 · el pedido a domicilio va de Confirmado a Recibido', { tag: ['@E2E-02', '@HU-024', '@HU-027', '@HU-028'] }, async ({ browser }) => {
  const { paginaTienda } = await entregarPedidoADomicilio(browser, 'E2E-02')
  // La tienda también ve el pedido cerrado
  await paginaTienda.reload()
  await expect(paginaTienda.getByText(/^Recibido · .*\(estado actual\)$/)).toBeVisible()
})
