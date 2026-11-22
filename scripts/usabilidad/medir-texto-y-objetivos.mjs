import { chromium } from 'playwright'
import { readFileSync } from 'node:fs'
const proxy = { server: process.env.HTTPS_PROXY }
const b = await chromium.launch({ proxy })
const { cuentas } = JSON.parse(readFileSync('/tmp/claude-0/carga/cuentas.json', 'utf8'))
const ctx = await b.newContext({ viewport: { width: 375, height: 812 }, isMobile: true, hasTouch: true, deviceScaleFactor: 1, geolocation: { latitude: 2.336, longitude: -76.7 }, permissions: ['geolocation'], locale: 'es-CO' })
const p = await ctx.newPage()
const medir = async (nombre) => {
  const r = await p.evaluate(() => {
    const base = parseFloat(getComputedStyle(document.body).fontSize)
    const textos = [...document.querySelectorAll('main p, main li, main label, main input, main button, main a, main dd, main dt')].filter(e => e.offsetParent)
    const chicos = textos.filter(e => parseFloat(getComputedStyle(e).fontSize) < 16).map(e => `${e.tagName}:${parseFloat(getComputedStyle(e).fontSize)}px:${(e.innerText||e.value||'').trim().slice(0,30)}`)
    const objetivos = [...document.querySelectorAll('button, a, input:not([type=hidden]), select, [role=radio], [role=switch]')].filter(e => e.offsetParent)
    const pequenos = objetivos.map(e => { const x = (e.closest('label') ?? e).getBoundingClientRect(); return { e, w: x.width, h: x.height } }).filter(o => (o.w < 44 || o.h < 44) && !o.e.closest('.leaflet-control-attribution') && o.e.getAttribute('href') !== '#contenido').map(o => `${o.e.tagName}${o.e.type ? '['+o.e.type+']' : ''} ${Math.round(o.w)}x${Math.round(o.h)} «${(o.e.innerText || o.e.getAttribute('aria-label') || '').trim().slice(0,30)}»`)
    return { base, textosRevisados: textos.length, menoresA16: chicos, objetivos: objetivos.length, menoresA44: pequenos }
  })
  console.log(`\n## ${nombre} (${p.url()})\ntexto base: ${r.base}px · elementos de texto: ${r.textosRevisados} · menores a 16 px: ${r.menoresA16.length}`)
  r.menoresA16.slice(0, 15).forEach(x => console.log('   texto', x))
  console.log(`objetivos táctiles: ${r.objetivos} · menores a 44x44: ${r.menoresA44.length}`)
  r.menoresA44.forEach(x => console.log('   objetivo', x))
}
for (const ruta of ['/', '/entrar', '/crear-cuenta', '/registrar-tienda']) { await p.goto('https://quienlotiene.tech' + ruta); await p.waitForTimeout(1500); await medir(ruta) }
await p.goto('https://quienlotiene.tech/entrar'); await p.getByLabel('Correo').fill(cuentas[1].correo); await p.getByLabel('Frase de contraseña').fill(cuentas[1].frase); await p.getByRole('button', { name: 'Entrar', exact: true }).click(); await p.waitForTimeout(2500)
for (const ruta of ['/solicitudes/nueva', '/solicitudes', '/pedidos', '/cuenta']) { await p.goto('https://quienlotiene.tech' + ruta); await p.waitForTimeout(2000); await medir(ruta) }
await b.close()
