import { expect, test } from '@playwright/test'
import { nuevaPagina, prepararTiendaAbierta, publicarSolicitud, registrarYEntrarCliente } from './apoyo/actores'

/**
 * E2E-05 · Solicitud sin respuestas que amplía el radio y vence (HU-021, HU-022).
 * Sin tiendas que respondan en 10 minutos, el radio pasa de 3 a 5 km una vez y a los 30 minutos la solicitud vence.
 *
 * Usa los tiempos aprobados (application.yml): 10 min para ampliar y 30 min de vigencia, más el minuto de las tareas
 * programadas. Si la API corre con otros valores, se pasan en E2E_ESPERA_AMPLIAR_MIN y E2E_VIGENCIA_MIN.
 */
const MINUTO = 60_000
const esperaAmpliar = Number(process.env.E2E_ESPERA_AMPLIAR_MIN ?? 10) * MINUTO
const vigencia = Number(process.env.E2E_VIGENCIA_MIN ?? 30) * MINUTO
// Popayán movido unos 4 km al norte: fuera del radio inicial de 3 km y dentro del ampliado de 5 km
const A_4_KM = { latitude: 2.4448 + 0.036, longitude: -76.6147 }

test('E2E-05 · sin respuestas el radio pasa a 5 km una vez y la solicitud vence', { tag: ['@E2E-05', '@HU-021', '@HU-022'] }, async ({ browser }) => {
  test.setTimeout(vigencia + 5 * MINUTO)

  // Una ferretería cerca que no responde y otra a 4 km que solo la recibe al ampliar
  const paginaTiendaCercana = await nuevaPagina(browser)
  await prepararTiendaAbierta(browser, paginaTiendaCercana, 'Ferretería')
  const paginaTiendaLejana = await nuevaPagina(browser, A_4_KM)
  await prepararTiendaAbierta(browser, paginaTiendaLejana, 'Ferretería')

  const paginaCliente = await nuevaPagina(browser)
  await registrarYEntrarCliente(paginaCliente)
  const producto = `Tornillo de 2 pulgadas E2E-05 ${Date.now() % 10000}`
  const publicada = Date.now()
  await publicarSolicitud(paginaCliente, producto, { tipo: 'Ferretería' })
  await expect(paginaCliente.getByText('Buscamos a 3 km a la redonda.')).toBeVisible()

  // A 4 km la tienda todavía no la ve
  await paginaTiendaLejana.goto('/tienda/solicitudes')
  await expect(paginaTiendaLejana.getByRole('heading', { name: 'Solicitudes', level: 1 })).toBeVisible()
  await expect(paginaTiendaLejana.getByRole('article', { name: producto })).toHaveCount(0)

  // HU-022: a los 10 minutos sin respuestas el radio pasa a 5 km y el cliente lo ve sin recargar
  const urlCliente = paginaCliente.url()
  await expect(paginaCliente.getByText('Buscamos a 5 km a la redonda.')).toBeVisible({ timeout: esperaAmpliar + 3 * MINUTO })
  const ampliadaA = Date.now() - publicada
  expect(ampliadaA).toBeGreaterThanOrEqual(esperaAmpliar - 5_000)
  expect(paginaCliente.url()).toBe(urlCliente)
  test.info().annotations.push({ type: 'ampliación', description: `a los ${(ampliadaA / MINUTO).toFixed(1)} min` })

  // La tienda a 4 km ahora sí la recibe
  await paginaTiendaLejana.reload()
  await expect(paginaTiendaLejana.getByRole('article', { name: producto })).toBeVisible()

  // HU-021: a los 30 minutos vence; el radio no se amplió una segunda vez
  await expect(paginaCliente.getByRole('heading', { name: 'Tu solicitud venció' })).toBeVisible({ timeout: vigencia - ampliadaA + 3 * MINUTO })
  const vencidaA = Date.now() - publicada
  expect(vencidaA).toBeGreaterThanOrEqual(vigencia - 5_000)
  test.info().annotations.push({ type: 'vencimiento', description: `a los ${(vencidaA / MINUTO).toFixed(1)} min` })
  // El título cambia con el reloj del navegador; el estado VENCIDA lo pone la tarea programada (cada minuto) y llega
  // por el canal de eventos. Hasta entonces la lista sigue con «Te avisamos…» (DEF-016)
  await expect(paginaCliente.getByText('Ninguna tienda la tenía disponible.')).toBeVisible({ timeout: 2 * MINUTO })
  test.info().annotations.push({ type: 'estado VENCIDA en pantalla', description: `a los ${((Date.now() - publicada) / MINUTO).toFixed(1)} min` })
  await paginaCliente.reload()
  await expect(paginaCliente.getByText('Buscamos a 5 km a la redonda.')).toBeVisible()
})
