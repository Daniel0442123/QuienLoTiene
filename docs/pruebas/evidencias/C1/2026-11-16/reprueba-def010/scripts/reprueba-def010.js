// Re-prueba de DEF-010 (16/11/2026, main e679ef3): los tres textos que llevan la hora, sin punto doble.
const { chromium, nuevo, cap, URL, caps } = require('./comun')
const fs = require('fs')
const { execSync } = require('child_process')
const out = {}
const sql = q => execSync(`docker exec qa-db psql -U quienlotiene -d quienlotiene -tAc "${q}"`).toString().trim()
async function paso(nombre, fn) { try { await fn(); out[nombre] = out[nombre] ?? 'ok' } catch (e) { out[nombre] = 'FALLO: ' + e.message.split('\n')[0]; console.log('FALLO PASO', nombre, e.message.split('\n')[0]) } }
const textoMain = p => p.locator('main').innerText()
const puntoDoble = t => t.split('\n').filter(l => /\.\./.test(l))
const PROD = 'Naproxeno 250 mg'
;(async () => {
  const b = await chromium.launch()
  const la5 = await nuevo(b, 'drogueria5@quienlotiene.test')
  await la5.goto(URL + '/tienda/solicitudes')
  const ana = await nuevo(b, 'ana@quienlotiene.test')
  await paso('publicar-y-responder', async () => {
    await ana.goto(URL + '/solicitudes/nueva'); await ana.waitForTimeout(1500)
    await ana.getByRole('textbox', { name: '¿Qué estás buscando?' }).fill(PROD)
    await ana.getByRole('radio', { name: 'Farmacia' }).check()
    await ana.getByRole('radio', { name: /^Casa/ }).check()
    await ana.getByRole('radio', { name: 'Domicilio' }).check()
    await ana.getByRole('button', { name: 'Preguntar', exact: true }).click(); await ana.waitForTimeout(2500)
    out.solicitudId = sql(`select id from solicitudes.solicitud where producto='${PROD}'`)
    await la5.getByText(PROD).first().waitFor({ timeout: 15000 })
    await la5.getByRole('link', { name: `Sí lo tengo: ${PROD}` }).click(); await la5.waitForTimeout(1000)
    out.urlResponder = la5.url()
    await la5.getByLabel('Precio por unidad').fill('7000')
    await la5.getByRole('button', { name: 'Enviar respuesta' }).click(); await la5.waitForTimeout(2000)
  })
  await paso('yaRespondiste', async () => {
    await la5.goto(out.urlResponder); await la5.waitForTimeout(2500)
    await cap(la5, 'def010-ya-respondiste', 'DEF-010: la tienda vuelve a la solicitud y lee «A las … respondiste esta solicitud con …», sin punto doble')
    const t = await textoMain(la5)
    out.yaRespondiste = t.split('\n').find(l => l.includes('respondiste')) ?? null
    out.yaRespondistePuntoDoble = puntoDoble(t)
  })
  await paso('entregadoPregunta', async () => {
    await ana.goto(URL + '/solicitudes/' + out.solicitudId); await ana.waitForTimeout(2500)
    const tarjeta = ana.locator('article, li').filter({ hasText: 'Droguería La 5' }).first()
    await tarjeta.getByRole('link', { name: /Elegir/ }).or(tarjeta.getByRole('button', { name: /Elegir/ })).first().click()
    await ana.waitForTimeout(2000)
    await ana.getByText('Efectivo', { exact: true }).click()
    await ana.getByRole('button', { name: /Confirmar/ }).last().click(); await ana.waitForTimeout(2500)
    await la5.goto(URL + '/tienda/pedidos'); await la5.waitForTimeout(2000)
    await la5.getByRole('link', { name: new RegExp(PROD) }).first().click(); await la5.waitForTimeout(2000)
    for (const btn of ['Empezar a preparar', 'Salió a domicilio', 'Marcar como entregado']) {
      await la5.getByRole('button', { name: btn }).click(); await la5.waitForTimeout(1500)
    }
    await ana.getByText(/marcó tu pedido como entregado/).waitFor({ timeout: 15000 }); await ana.waitForTimeout(1000)
    await cap(ana, 'def010-entregado-pregunta', 'DEF-010: el cliente lee «A las …, Droguería La 5 marcó tu pedido como entregado. ¿Ya lo tienes?»')
    const t = await textoMain(ana)
    out.entregadoPregunta = t.split('\n').find(l => l.includes('marcó tu pedido')) ?? null
    out.entregadoPuntoDoble = puntoDoble(t)
  })
  await paso('plazoTerminado', async () => {
    const luis = await nuevo(b, 'luis@quienlotiene.test')
    await luis.goto(URL + '/solicitudes/nueva'); await luis.waitForTimeout(1500)
    await luis.getByRole('textbox', { name: '¿Qué estás buscando?' }).fill('Diclofenaco 50 mg')
    await luis.getByRole('radio', { name: 'Farmacia' }).check()
    await luis.getByRole('radio', { name: /recojo/ }).check()
    await luis.getByRole('button', { name: 'Preguntar', exact: true }).click(); await luis.waitForTimeout(2500)
    const id = sql(`select id from solicitudes.solicitud where producto='Diclofenaco 50 mg'`)
    sql(`update solicitudes.solicitud set vence_en = now() - interval '1 second' where id='${id}'`)
    await luis.getByRole('heading', { level: 1, name: 'Tu solicitud venció' }).waitFor({ timeout: 90000 }); await luis.waitForTimeout(1500)
    await cap(luis, 'def010-plazo-terminado', 'DEF-010: «A las … terminó el plazo para responder.», sin punto doble')
    const t = await textoMain(luis)
    out.plazoTerminado = t.split('\n').find(l => l.includes('terminó el plazo')) ?? null
    out.plazoPuntoDoble = puntoDoble(t)
  })
  fs.writeFileSync('reprueba-def010-salida.json', JSON.stringify(out, null, 1))
  await b.close()
})().catch(e => { console.error(e); process.exit(1) })
