const { chromium, nuevo, cap, URL, caps, setN } = require('./comun')
const fs = require('fs'); const { execSync } = require('child_process')
setN(27)
const out = {}
async function paso(nombre, fn) { try { await fn() } catch (e) { console.log('FALLO PASO', nombre, e.message.split('\n')[0]) } }
const URG = 'Amoxicilina 500 mg', NOR = 'Ibuprofeno 400 mg'
async function publicar(p, prod, urgente, domicilio) {
  await p.goto(URL + '/solicitudes/nueva'); await p.waitForTimeout(1500)
  await p.getByRole('textbox', { name: '¿Qué estás buscando?' }).fill(prod)
  await p.getByRole('radio', { name: 'Farmacia' }).check()
  await p.getByRole('radio', { name: /^Casa/ }).check()
  if (domicilio) await p.getByRole('radio', { name: 'Domicilio' }).check()
  if (urgente) {
    await p.locator('summary').focus(); await p.keyboard.press('Enter'); await p.waitForTimeout(400)
    if (!(await p.locator('details').evaluate(d => d.open))) await p.locator('details').evaluate(d => { d.open = true })
    await p.getByRole('checkbox', { name: 'Lo necesito con urgencia' }).check()
  }
  await p.getByRole('button', { name: 'Preguntar', exact: true }).click(); await p.waitForTimeout(2000)
}
;(async () => {
  const b = await chromium.launch()
  const la5 = await nuevo(b, 'drogueria5@quienlotiene.test')
  await la5.addInitScript(() => {})
  await la5.evaluate(() => {})
  // espía del sonido: cuenta los osciladores que arranca la alerta
  await la5.context().addInitScript(() => {
    window.__alertas = 0
    const orig = AudioContext.prototype.createOscillator
    AudioContext.prototype.createOscillator = function () { window.__alertas++; return orig.call(this) }
  })
  await la5.goto(URL + '/tienda/solicitudes'); await la5.waitForTimeout(2500)
  const luisA = await nuevo(b, 'luis@quienlotiene.test')
  const luisB = await luisA.context().newPage()
  await paso('urgente-y-normal', async () => {
    await publicar(luisA, URG, true, false)
    await publicar(luisB, NOR, false, false)
    await la5.getByText(NOR).first().waitFor({ timeout: 15000 }); await la5.waitForTimeout(1500)
    out.alertas = await la5.evaluate(() => window.__alertas)
    out.ordenTarjetas = await la5.locator('article h2').allInnerTexts()
    await cap(la5, 'tienda-urgente-primero', `CP-NOT-030-02: se publican una urgente y luego una normal; la urgente queda primero (osciladores de la alerta: ${out.alertas})`)
  })
  await paso('privacidad-api', async () => {
    out.disponiblesApi = execSync('python3 -c "from api import *; t=login(\'botica@quienlotiene.test\',\'frase segura de pruebas qa sprint dos\')[\'tokenAcceso\']; import json; print(json.dumps(req(\'GET\',\'/solicitudes/disponibles\',tok=t),ensure_ascii=False))"').toString()
  })
  await paso('responder-urgente', async () => {
    await la5.getByRole('link', { name: `Sí lo tengo: ${URG}` }).click(); await la5.waitForTimeout(1000)
    out.solUrgente = la5.url().split('/').pop()
    await la5.getByLabel('Precio por unidad').fill('12000')
    await la5.getByRole('button', { name: 'Enviar respuesta' }).click(); await la5.waitForTimeout(1500)
    await la5.goto(URL + '/tienda/solicitudes'); await la5.waitForTimeout(1500)
    await la5.getByRole('link', { name: `Sí lo tengo: ${NOR}` }).click(); await la5.waitForTimeout(1000)
    out.solNormal = la5.url().split('/').pop()
    await la5.getByLabel('Precio por unidad').fill('6000')
    await la5.getByRole('button', { name: 'Enviar respuesta' }).click(); await la5.waitForTimeout(1500)
  })
  await paso('medio-no-aceptado', async () => {
    await luisA.goto(URL + '/solicitudes/' + out.solUrgente); await luisA.waitForTimeout(2000)
    await cap(luisA, 'cliente-solicitud-urgente-respuestas', 'Luis abre su solicitud urgente por URL y ve la respuesta de Droguería La 5')
    await luisA.getByRole('link', { name: /Elegir/ }).or(luisA.getByRole('button', { name: /Elegir/ })).first().click(); await luisA.waitForTimeout(2000)
    await luisA.getByText('Datáfono', { exact: true }).click()
    await luisA.getByRole('button', { name: /Confirmar/ }).last().click(); await luisA.waitForTimeout(2500)
    await cap(luisA, 'cliente-medio-no-aceptado', 'CP-ORD-024-04 / CP-TIE-012-01: la pantalla ofrece Datáfono aunque La 5 solo acepta efectivo y transferencia; el backend rechaza la orden')
    out.medioNoAceptado = await luisA.locator('main').innerText()
    await luisA.getByText('Transferencia', { exact: true }).click()
    await luisA.getByRole('button', { name: /Confirmar/ }).last().click(); await luisA.waitForTimeout(2500)
    out.ordenLuis = luisA.url().split('/').pop()
    await cap(luisA, 'cliente-pedido-recoger', 'Con transferencia la orden se crea (recoger en la tienda)')
  })
  await paso('vencimiento', async () => {
    await luisB.waitForTimeout(500)
    await cap(luisB, 'cliente-solicitud-abierta-antes-de-vencer', 'Solicitud normal de Luis abierta, con la respuesta de La 5, antes de vencer')
    execSync(`docker exec qa-db psql -U quienlotiene -d quienlotiene -c "update solicitudes.solicitud set vence_en = now() - interval '1 second' where id='${out.solNormal}'"`)
    await luisB.getByText('Vencida', { exact: false }).first().waitFor({ timeout: 90000 })
    await luisB.waitForTimeout(1000)
    await cap(luisB, 'cliente-solicitud-vencida', 'CP-SOL-021-01: la tarea de vencimiento la pasa a VENCIDA y el cliente lo ve sin recargar; ya no puede elegir')
    out.vencida = await luisB.locator('main').innerText()
    await la5.goto(URL + '/tienda/solicitudes/mis-respuestas'); await la5.waitForTimeout(2000)
    await cap(la5, 'tienda-respuesta-vencida', 'La tienda ve su respuesta a la solicitud vencida como «Vencida»')
  })
  fs.writeFileSync('flujo2-salida.json', JSON.stringify(out, null, 1))
  fs.writeFileSync('caps2.json', JSON.stringify(caps, null, 1))
  await b.close()
})().catch(e => { console.error(e); process.exit(1) })
