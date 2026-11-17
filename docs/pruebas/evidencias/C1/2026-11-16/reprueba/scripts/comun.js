const { chromium } = require('playwright')
const F = 'frase segura de pruebas qa sprint dos'
const URL = 'http://127.0.0.1:5173'
let n = 0
const caps = []
async function nuevo(browser, correo) {
  const ctx = await browser.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 2, locale: 'es-CO', timezoneId: 'America/Bogota', geolocation: { latitude: 2.4448, longitude: -76.6147 }, permissions: ['geolocation'] })
  const p = await ctx.newPage()
  p.on('pageerror', e => console.log('PAGEERROR', correo, e.message))
  p.on('console', m => { if (m.type() === 'error') console.log('CONSOLE', correo, m.text().slice(0, 200)) })
  await p.goto(URL + '/entrar')
  await p.getByLabel('Correo').fill(correo)
  await p.getByLabel('Frase de contraseña', { exact: true }).fill(F)
  await p.getByRole('button', { name: 'Entrar', exact: true }).click()
  await p.waitForURL(u => !u.pathname.startsWith('/entrar'), { timeout: 15000 })
  await p.waitForTimeout(1500)
  return p
}
async function cap(p, nombre, desc, full = true) {
  n++
  const f = `${String(n).padStart(2, '0')}-${nombre}.png`
  await p.waitForTimeout(400)
  await p.screenshot({ path: 'caps2/' + f, fullPage: full })
  caps.push([f, desc]); console.log('CAP', f, '|', desc)
}
module.exports = { chromium, nuevo, cap, URL, caps, setN: v => (n = v) }
