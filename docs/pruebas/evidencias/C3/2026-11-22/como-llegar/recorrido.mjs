// Guion usado para el recorrido de CP-ORD-026-01 en producción (22/11/2026). Lee las cuentas de prueba de /tmp, que no se suben.
import { chromium } from '@playwright/test'
import { readFileSync } from 'node:fs'
const D = process.argv[2]
const BASE = 'https://quienlotiene.tech'
const t = JSON.parse(readFileSync('/tmp/claude-0/carga/tienda-carga.json', 'utf8'))
const c = JSON.parse(readFileSync('/tmp/claude-0/carga/cuentas.json', 'utf8')).cuentas[2]
const b = await chromium.launch({ proxy: { server: process.env.HTTPS_PROXY } })
const nuevo = async (geo) => (await b.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 2, isMobile: true, hasTouch: true, geolocation: geo, permissions: ['geolocation'], locale: 'es-CO', timezoneId: 'America/Bogota' })).newPage()
const log = (m) => console.log(`${new Date().toISOString()} ${m}`)
const entrar = async (p, correo, frase) => { await p.goto(BASE + '/entrar'); await p.getByLabel('Correo').fill(correo); await p.getByLabel('Frase de contraseña').fill(frase); await p.getByRole('button', { name: 'Entrar', exact: true }).click(); await p.waitForURL((u) => !u.pathname.startsWith('/entrar')) }
try {
const pt = await nuevo({ latitude: t.lat, longitude: t.lon })
await entrar(pt, t.correo, t.frase); log('la tienda de prueba de carga entra')
await pt.goto(BASE + '/tienda'); const sw = pt.getByRole('switch', { name: 'Recibir solicitudes' }); await sw.waitFor(); if (!(await sw.isChecked())) await sw.click(); log('la tienda queda abierta')
await pt.goto(BASE + '/tienda/medios-pago'); const ef = pt.getByRole('checkbox', { name: 'Efectivo' }); await ef.waitFor(); if (!(await ef.isChecked())) { await ef.check(); await pt.getByRole('button', { name: 'Guardar cambios' }).click(); await pt.getByText('Medios de pago actualizados.').waitFor() } log('la tienda acepta efectivo')
const pc = await nuevo({ latitude: t.lat + 0.005, longitude: t.lon })
await entrar(pc, c.correo, c.frase); log('el cliente de prueba entra')
const producto = `Acetaminofén 500 mg (recorrido Cómo llegar ${Date.now() % 10000})`
await pc.goto(BASE + '/solicitudes/nueva'); await pc.getByRole('textbox', { name: '¿Qué estás buscando?' }).fill(producto); await pc.getByRole('radio', { name: 'Farmacia' }).check()
await pc.getByRole('button', { name: 'Usar mi ubicación' }).click()
await pc.getByRole('radio', { name: 'Lo recojo en la tienda' }).check(); await pc.getByRole('button', { name: 'Preguntar' }).click(); await pc.waitForURL(/solicitud=/); log('el cliente publica «' + producto + '» para recoger')
await pt.goto(BASE + '/tienda/solicitudes'); await pt.getByRole('link', { name: `Sí lo tengo: ${producto}` }).click(); await pt.getByRole('textbox', { name: 'Precio por unidad' }).fill('8000'); await pt.getByRole('button', { name: 'Enviar respuesta' }).click(); log('la tienda responde Sí lo tengo a $ 8.000')
await pc.getByRole('link', { name: 'Elegir Tienda de prueba de carga' }).click(); await pc.getByRole('heading', { name: 'Confirmar pedido' }).waitFor()
const efectivo = pc.getByRole('radio', { name: 'Efectivo' }); if (await efectivo.count()) await efectivo.check()
await pc.getByRole('button', { name: /Confirmar pedido/ }).click(); await pc.waitForURL(/\/pedidos\/[0-9a-f-]{36}/); log('el cliente confirma el pedido para recoger: ' + pc.url())
const boton = pc.getByRole('link', { name: /Cómo llegar/ }); await boton.waitFor(); await pc.screenshot({ path: `${D}/1-pedido-con-como-llegar.png`, fullPage: true })
log(`enlace «${(await boton.textContent()).trim()}», target=${await boton.getAttribute('target')}, rel=${await boton.getAttribute('rel')}, alto ${Math.round((await boton.boundingBox()).height)} px`)
log('href: ' + await boton.getAttribute('href'))
const [popup] = await Promise.all([pc.waitForEvent('popup'), boton.click()]); log('al tocarlo se abre otra pestaña con: ' + popup.url())
log(`ubicación registrada de la tienda: ${t.lat}, ${t.lon}`)
} catch (e) { log('ERROR ' + e.message.split('\n')[0]); process.exitCode = 1 } finally { await b.close() }
