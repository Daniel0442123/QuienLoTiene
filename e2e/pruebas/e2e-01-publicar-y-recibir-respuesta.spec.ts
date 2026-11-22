import { expect, test } from '@playwright/test'
import { nuevaPagina, prepararTiendaAbierta, publicarSolicitud, registrarYEntrarCliente, responderSiLoTengo } from './apoyo/actores'

/**
 * E2E-01 · Publicar y recibir respuesta (HU-001, HU-005, HU-016, HU-017, HU-019, HU-020).
 * Cliente se registra, publica una solicitud y una tienda aprobada y abierta responde «Sí lo tengo»; la respuesta
 * aparece en la pantalla del cliente sin recargar.
 */
test('E2E-01 · la respuesta de la tienda aparece al cliente sin recargar', { tag: ['@E2E-01', '@HU-016', '@HU-019', '@HU-020'] }, async ({ browser }) => {
  const paginaTienda = await nuevaPagina(browser)
  const paginaCliente = await nuevaPagina(browser)
  const tienda = await prepararTiendaAbierta(browser, paginaTienda)

  await registrarYEntrarCliente(paginaCliente)
  const producto = `Acetaminofén 500 mg E2E-01 ${Date.now() % 10000}`
  await publicarSolicitud(paginaCliente, producto)
  await expect(paginaCliente.getByRole('heading', { name: 'Esperando respuestas', level: 1 })).toBeVisible()
  await expect(paginaCliente.getByRole('heading', { name: '0 respuestas' })).toBeVisible()

  // La tienda la recibe en tiempo real y responde
  await expect(paginaTienda.getByRole('status').filter({ hasText: `Solicitud nueva: «${producto}».` })).toBeAttached()
  await responderSiLoTengo(paginaTienda, producto, 8000)
  await expect(paginaTienda.getByText('Sí lo tengo · $ 8.000 · 1 unidad').first()).toBeVisible()

  // Sin recargar: la pantalla del cliente se actualiza por el canal de eventos (SSE)
  const urlAntes = paginaCliente.url()
  const respuesta = paginaCliente.getByRole('article', { name: tienda.nombre })
  await expect(paginaCliente.getByRole('heading', { name: '1 respuesta' })).toBeVisible()
  await expect(respuesta.getByText('Sí lo tengo')).toBeVisible()
  await expect(respuesta).toContainText(/Total\s*\$\s8\.000/)
  await expect(respuesta.getByRole('link', { name: `Elegir ${tienda.nombre}` })).toBeVisible()
  expect(paginaCliente.url()).toBe(urlAntes)
})
