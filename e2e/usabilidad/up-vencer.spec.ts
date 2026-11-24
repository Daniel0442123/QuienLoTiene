import { test, expect } from '@playwright/test'
import { caso } from './registro'
import { FRASE, correoUnico, entrar, publicarSolicitud } from '../pruebas/apoyo/actores'
test('UP-CLI-04-E2 · la solicitud vence sin elegir', async ({ browser }) => {
  const c = await (await browser.newContext({ viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true })).newPage()
  const correo = correoUnico('up-vence')
  await c.goto('/crear-cuenta')
  await c.getByLabel('Nombre').fill('Cliente Vence'); await c.getByLabel('Correo').fill(correo)
  await c.getByLabel('Celular').fill('3109876543'); await c.getByLabel('Frase de contraseña').fill(FRASE)
  await c.getByRole('checkbox', { name: /Autorizo el tratamiento/ }).check(); await c.getByRole('button', { name: 'Crear cuenta' }).click()
  await expect(c.getByRole('main').getByText(/^Cuenta creada./)).toBeVisible()
  await entrar(c, correo, FRASE); await c.waitForURL((u) => !u.pathname.startsWith('/entrar'))
  await publicarSolicitud(c, `Repuesto inexistente UP ${Date.now() % 10000}`, { tipo: 'Ferretería' })
  await caso('UP-CLI-04-E2', c, async () => {
    await expect(c.getByRole('heading', { name: 'Tu solicitud venció' })).toBeVisible({ timeout: 300_000 })
    const main = (await c.getByRole('main').innerText()).replace(/\s+/g, ' ')
    const ofrece = /(Preguntar de nuevo|Volver a preguntar|Repetir)/i.test(main)
    return { ok: ofrece, nota: `Pantalla «Tu solicitud venció»; ofrece volver a preguntar: ${ofrece ? 'sí' : 'no'} · ${main.slice(0, 200)}` }
  })
})
