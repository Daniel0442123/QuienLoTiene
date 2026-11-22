import { expect, test } from '@playwright/test'
import { entregarPedidoADomicilio } from './apoyo/flujos'

/**
 * E2E-04 · Calificar después de la entrega (HU-028, HU-034).
 * Tras confirmar la recepción, el cliente califica la tienda con 1 a 5 estrellas y la calificación se ve en el detalle
 * de la tienda.
 */
test('E2E-04 · la calificación del cliente se ve en el detalle de la tienda', { tag: ['@E2E-04', '@HU-028', '@HU-034'] }, async ({ browser }) => {
  const { paginaCliente, tienda } = await entregarPedidoADomicilio(browser, 'E2E-04')

  await paginaCliente.getByRole('link', { name: new RegExp(`Calificar a ${tienda.nombre}`) }).click()
  await expect(paginaCliente.getByRole('heading', { name: `¿Cómo te fue con ${tienda.nombre}?` })).toBeVisible()
  await paginaCliente.getByRole('radio', { name: '4 estrellas' }).check({ force: true }) // el radio es sr-only bajo la estrella
  await paginaCliente.getByLabel('Comentario (opcional)').fill('Llegó rápido y bien empacado.')
  await paginaCliente.getByRole('button', { name: 'Publicar calificación' }).click()
  await expect(paginaCliente.getByText('Gracias, tu calificación quedó publicada.').first()).toBeVisible()

  await paginaCliente.getByRole('link', { name: tienda.nombre }).first().click()
  await expect(paginaCliente.getByText('4,0 de 5 · 1 reseña').or(paginaCliente.getByText('4 de 5 · 1 reseña')).first()).toBeVisible()
  await expect(paginaCliente.getByText('Llegó rápido y bien empacado.')).toBeVisible()
})
