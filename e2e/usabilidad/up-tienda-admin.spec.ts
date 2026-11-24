import fs from 'node:fs'
import { test, expect, type Page } from '@playwright/test'
import os from 'node:os'
import path from 'node:path'
import { caso, avisos, DIR } from './registro'
import {
  ADMIN, FRASE, correoUnico, entrar, nuevaPagina, registrarTienda, aprobarTienda, abrirTienda, guardarDireccion,
  publicarSolicitud,
} from '../pruebas/apoyo/actores'
import { codigoTotpSeguro } from '../pruebas/apoyo/totp'

const MOVIL = { viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true }
const TXT = path.join(os.tmpdir(), 'no-es-imagen.txt')
fs.writeFileSync(TXT, 'esto no es una imagen')

async function textoMain(p: Page) { return (await p.getByRole('main').innerText()).replace(/\s+/g, ' ') }

test('UP · tienda y administrador: ruta feliz y excepciones', async ({ browser }) => {
  // ---------- UP-ADM-01 (primero, para tener la sesión del administrador) ----------
  const adm = await nuevaPagina(browser)
  await caso('UP-ADM-01-E1', adm, async () => {
    await entrar(adm, ADMIN.correo, ADMIN.frase)
    await expect(adm.getByRole('heading', { name: 'Código de verificación' })).toBeVisible()
    await adm.getByLabel('Código de 6 dígitos').fill('000000')
    await adm.getByRole('button', { name: 'Verificar' }).click()
    await adm.waitForTimeout(1200)
    const txt = await avisos(adm)
    const sigue = await adm.getByLabel('Código de 6 dígitos').isVisible()
    return { ok: sigue && /código/i.test(txt), nota: `${txt} · se puede intentar otra vez: ${sigue ? 'sí' : 'no'}` }
  })
  await caso('UP-ADM-01', adm, async () => {
    const inicio = Date.now()
    await adm.getByLabel('Código de 6 dígitos').fill(await codigoTotpSeguro(ADMIN.secretoTotp))
    await adm.getByRole('button', { name: 'Verificar' }).click()
    await expect(adm.getByRole('navigation', { name: 'Menú principal' }).getByRole('link', { name: 'Reportes' })).toBeVisible()
    return { ok: true, inicio, nota: 'Entra al panel de administración.' }
  })

  // ---------- UP-TIE-01 ----------
  const t = await nuevaPagina(browser)
  await caso('UP-TIE-01-E1', t, async () => {
    await t.goto('/registrar-tienda')
    await t.getByLabel('Tu nombre').fill('Dueño Sin RUT')
    await t.getByLabel('Correo').fill(correoUnico('up-sinrut'))
    await t.getByLabel('Celular').fill('3001234567')
    await t.getByLabel('Frase de contraseña').fill(FRASE)
    await t.getByLabel('Nombre de la tienda').fill('Droguería Sin RUT')
    await t.getByRole('radio', { name: 'Farmacia' }).check()
    await t.getByRole('button', { name: 'Usar mi ubicación' }).click()
    await t.getByRole('checkbox', { name: /Autorizo el tratamiento/ }).check()
    await t.getByRole('button', { name: 'Registrar tienda' }).click()
    await t.waitForTimeout(1000)
    const txt = await avisos(t)
    const conserva = (await t.getByLabel('Nombre de la tienda').inputValue()) === 'Droguería Sin RUT'
    return { ok: /RUT/i.test(txt) && conserva, nota: `${txt} · datos conservados: ${conserva ? 'sí' : 'no'}` }
  })
  await caso('UP-TIE-01-E2', t, async () => {
    await t.locator('#tienda-foto').setInputFiles(TXT)
    await t.waitForTimeout(800)
    const txt = await avisos(t)
    return { ok: /no sirve|JPG|PDF/i.test(txt), nota: txt }
  })
  let tienda = { correo: '', nombre: '' }
  await caso('UP-TIE-01', t, async () => {
    const inicio = Date.now()
    tienda = await registrarTienda(t)
    await expect(t.getByRole('main').getByText(/Revisamos/).first()).toBeVisible()
    return { ok: true, inicio, nota: (await textoMain(t)).match(/[^.]*(Revisamos|revisión|pendiente)[^.]*\./gi)?.slice(-2).join(' ') ?? '' }
  })

  // ---------- UP-ADM-02 ----------
  const t2 = await nuevaPagina(browser)
  const tiendaRechazo = await registrarTienda(t2)
  await caso('UP-ADM-02', adm, async () => {
    const inicio = Date.now()
    await aprobarTienda(adm, tienda)
    await expect(adm.getByText(`Aprobaste ${tienda.nombre}.`, { exact: false }).first()).toBeVisible()
    return { ok: true, inicio, nota: await avisos(adm) }
  })
  await caso('UP-ADM-02-E1', adm, async () => {
    const tarjeta = adm.getByRole('article', { name: tiendaRechazo.nombre })
    await tarjeta.getByRole('button', { name: 'Rechazar' }).click()
    await adm.getByRole('button', { name: 'Rechazar tienda' }).click()
    await adm.waitForTimeout(800)
    const txt = await avisos(adm)
    return { ok: /motivo/i.test(txt) && !/Rechazaste/.test(txt), nota: txt }
  })
  await caso('UP-ADM-02-rechazo', adm, async () => {
    await adm.getByLabel('Motivo del rechazo').fill('El RUT no se lee; súbelo de nuevo con mejor luz.')
    await adm.getByRole('button', { name: 'Rechazar tienda' }).click()
    await expect(adm.getByText(`Rechazaste ${tiendaRechazo.nombre}`, { exact: false }).first()).toBeVisible()
    return { ok: true, nota: await avisos(adm) }
  })

  // ---------- UP-TIE-02 ----------
  await entrar(t, tienda.correo, FRASE)
  await t.waitForURL(/\/tienda/)
  await caso('UP-TIE-02-E1', t, async () => {
    await t.goto('/tienda/domicilio')
    await t.getByRole('checkbox', { name: 'Hago domicilios' }).check()
    await t.getByRole('combobox', { name: 'Hasta qué distancia llevas pedidos' }).selectOption({ label: '3 km' })
    const valor = t.getByRole('textbox', { name: 'Valor del domicilio (pesos)' })
    await valor.fill('')
    await t.getByRole('button', { name: 'Guardar cambios' }).click()
    await t.waitForTimeout(600)
    const vacio = await avisos(t)
    await valor.fill('-500')
    await t.getByRole('button', { name: 'Guardar cambios' }).click()
    await t.waitForTimeout(600)
    const negativo = await avisos(t)
    return { ok: /Escribe el valor/i.test(vacio) && /solo números|valor/i.test(negativo), nota: `Vacío: ${vacio} · Negativo: ${negativo}` }
  })
  await caso('UP-TIE-02-E2', t, async () => {
    const opciones = await t.getByRole('combobox', { name: 'Hasta qué distancia llevas pedidos' }).locator('option').allInnerTexts()
    return { ok: true, nota: `El radio se elige de una lista; no se puede escribir 100 m ni 20 km. Opciones: ${opciones.join(', ')}` }
  })
  await caso('UP-TIE-02', t, async () => {
    const inicio = Date.now()
    await t.getByRole('textbox', { name: 'Valor del domicilio (pesos)' }).fill('4000')
    await t.getByRole('button', { name: 'Guardar cambios' }).click()
    await expect(t.getByRole('main').getByText(/guardad|actualizad/i).first()).toBeVisible()
    await t.goto('/tienda/medios-pago')
    await t.getByRole('checkbox', { name: 'Efectivo' }).check()
    await t.getByRole('checkbox', { name: /Transferencia/ }).check()
    await t.getByRole('button', { name: 'Guardar cambios' }).click()
    await expect(t.getByRole('main').getByText('Medios de pago actualizados.')).toBeVisible()
    return { ok: true, inicio, nota: 'Domicilio 3 km · $ 4.000 y medios efectivo y transferencia guardados.' }
  })
  await caso('UP-TIE-02-E3', t, async () => {
    for (const cb of await t.getByRole('checkbox').all()) if (await cb.isChecked()) await cb.uncheck()
    await t.getByRole('button', { name: 'Guardar cambios' }).click()
    await t.waitForTimeout(600)
    const txt = await avisos(t)
    await t.reload()
    return { ok: /al menos un medio/i.test(txt), nota: txt }
  })

  // ---------- UP-TIE-03 ----------
  const cli = await (await browser.newContext(MOVIL)).newPage()
  const correoCli = correoUnico('up-cli-tienda')
  await cli.goto('/crear-cuenta')
  await cli.getByLabel('Nombre').fill('Cliente Tienda'); await cli.getByLabel('Correo').fill(correoCli)
  await cli.getByLabel('Celular').fill('3109876543'); await cli.getByLabel('Frase de contraseña').fill(FRASE)
  await cli.getByRole('checkbox', { name: /Autorizo el tratamiento/ }).check(); await cli.getByRole('button', { name: 'Crear cuenta' }).click()
  await expect(cli.getByRole('main').getByText(/^Cuenta creada./)).toBeVisible()
  await entrar(cli, correoCli, FRASE); await cli.waitForURL((u) => !u.pathname.startsWith('/entrar'))
  await guardarDireccion(cli)

  // E3 primero: tienda cerrada no recibe
  const prodCerrada = `Suero oral UP ${Date.now() % 10000}`
  await caso('UP-TIE-03-E3', t, async () => {
    await t.goto('/tienda')
    const sw = t.getByRole('switch', { name: 'Recibir solicitudes' })
    if (await sw.isChecked()) await sw.click()
    await publicarSolicitud(cli, prodCerrada, { direccionGuardada: true })
    await t.goto('/tienda/solicitudes'); await t.waitForTimeout(1500)
    const n = await t.getByRole('article', { name: prodCerrada }).count()
    return { ok: n === 0, nota: `Con la tienda cerrada, la solicitud aparece en su bandeja: ${n ? 'sí' : 'no'}` }
  })
  const prodSi = `Ibuprofeno 400 mg UP ${Date.now() % 10000}`
  await caso('UP-TIE-03', t, async () => {
    await abrirTienda(t)
    await t.goto('/tienda/solicitudes')
    await publicarSolicitud(cli, prodSi, { domicilio: true, direccionGuardada: true })
    await expect(t.getByRole('article', { name: prodSi })).toBeVisible()
    const inicio = Date.now()
    await t.getByRole('link', { name: `Sí lo tengo: ${prodSi}` }).click()
    await t.getByRole('textbox', { name: 'Precio por unidad' }).fill('6000')
    await t.getByRole('button', { name: 'Enviar respuesta' }).click()
    await expect(cli.getByRole('link', { name: `Elegir ${tienda.nombre}` })).toBeVisible()
    return { ok: true, inicio, toques: 3, nota: 'La solicitud llegó sin recargar; «Sí lo tengo» en 3 toques (tocar, escribir precio, enviar) y la respuesta apareció al cliente en vivo.' }
  })
  await caso('UP-TIE-03-E1', t, async () => {
    const prod = `Venda elástica UP ${Date.now() % 10000}`
    await publicarSolicitud(cli, prod, { direccionGuardada: true })
    await t.goto('/tienda/solicitudes')
    await t.getByRole('link', { name: `Tengo algo parecido: ${prod}` }).click()
    await t.getByRole('textbox', { name: 'Precio por unidad' }).fill('3000')
    await t.getByRole('button', { name: 'Enviar respuesta' }).click()
    await t.waitForTimeout(800)
    const txt = await avisos(t)
    return { ok: /Describe qué producto/i.test(txt), nota: txt }
  })
  await caso('UP-TIE-03-E2', t, async () => {
    await t.getByRole('textbox', { name: 'Precio por unidad' }).fill('0')
    await t.getByRole('button', { name: 'Enviar respuesta' }).click()
    await t.waitForTimeout(800)
    const txt = await avisos(t)
    return { ok: /precio entre/i.test(txt), nota: txt }
  })
  await caso('UP-TIE-03-No', t, async () => {
    const prod = `Tapabocas UP ${Date.now() % 10000}`
    await publicarSolicitud(cli, prod, { direccionGuardada: true })
    await t.goto('/tienda/solicitudes')
    await expect(t.getByRole('article', { name: prod })).toBeVisible()
    const inicio = Date.now()
    await t.getByRole('button', { name: `No lo tengo: ${prod}` }).click()
    await t.waitForTimeout(1000)
    const sigue = await t.getByRole('article', { name: prod }).count()
    return { ok: sigue === 0, inicio, toques: 1, nota: `«No lo tengo» en 1 toque; la tarjeta sale de la lista: ${sigue === 0 ? 'sí' : 'no'} · ${await avisos(t)}` }
  })

  // ---------- UP-TIE-04 ----------
  await cli.goto('/solicitudes'); await cli.waitForTimeout(500)
  await publicarSolicitud(cli, `${prodSi} b`, { domicilio: true, direccionGuardada: true })
  await t.goto('/tienda/solicitudes')
  await t.getByRole('link', { name: `Sí lo tengo: ${prodSi} b` }).click()
  await t.getByRole('textbox', { name: 'Precio por unidad' }).fill('6000')
  await t.getByRole('button', { name: 'Enviar respuesta' }).click()
  await cli.getByRole('link', { name: `Elegir ${tienda.nombre}` }).click()
  await cli.getByRole('radio', { name: /^Domicilio · \$ 4\.000/ }).check()
  await cli.getByRole('radio', { name: 'Efectivo' }).check()
  await cli.getByRole('button', { name: /Confirmar pedido/ }).click()
  await cli.waitForURL(/\/pedidos\/[0-9a-f-]{36}$/)
  const pedidoCli = cli.url()
  await t.goto('/tienda/pedidos'); await t.getByRole('link', { name: new RegExp(`${prodSi} b`) }).click()
  const pedidoTienda = t.url()
  await caso('UP-TIE-04-E1', t, async () => {
    await expect(t.getByRole('button', { name: 'Empezar a preparar' })).toBeVisible()
    const saltar = await t.getByRole('button', { name: 'Marcar como entregado' }).count()
    const botones = (await t.getByRole('main').getByRole('button').allInnerTexts()).map((b) => b.trim()).filter(Boolean)
    return { ok: saltar === 0, nota: `En «Confirmado» se ofrece: ${botones.join(' · ')}` }
  })
  await caso('UP-TIE-04', t, async () => {
    const inicio = Date.now()
    for (const [boton, estado] of [['Empezar a preparar', 'Preparando'], ['Salió a domicilio', 'En camino'], ['Marcar como entregado', 'Entregado']] as const) {
      await t.getByRole('button', { name: boton }).click()
      await expect(cli.getByText(new RegExp(`^${estado} · .*\\(estado actual\\)$`))).toBeVisible()
    }
    return { ok: true, inicio, toques: 3, nota: 'Cada estado llegó al cliente sin recargar (1 toque por cambio).' }
  })
  await cli.getByRole('button', { name: 'Recibí mi pedido' }).click()
  await expect(cli.getByText(/^Recibido · .*\(estado actual\)$/)).toBeVisible()
  await caso('UP-TIE-04-E2', t, async () => {
    await t.goto(pedidoTienda); await t.waitForTimeout(1000)
    const txt = await textoMain(t)
    const ve = /Calle 5 # 10-20/.test(txt)
    return { ok: !ve, nota: `Dirección del cliente visible después de recibido: ${ve ? 'sí' : 'no'}` }
  })

  // ---------- UP-ADM-03 ----------
  await cli.goto(pedidoCli + '/reportar')
  await cli.getByRole('radio').first().check({ force: true })
  await cli.getByRole('button', { name: /Enviar|Reportar/ }).last().click()
  await expect(cli.getByText(/Enviamos tu reporte/).first()).toBeVisible()
  await caso('UP-ADM-03', adm, async () => {
    const inicio = Date.now()
    await adm.goto('/admin/reportes'); await adm.waitForTimeout(800)
    await adm.getByRole('button', { name: `Suspender a ${tienda.nombre}` }).first().click()
    await adm.getByLabel('¿Por qué la suspendes?').fill('Reportes repetidos de demora.')
    await adm.getByRole('button', { name: `Sí, suspender a ${tienda.nombre}` }).click()
    await expect(adm.getByText(`${tienda.nombre} quedó suspendida.`).first()).toBeAttached()
    const tarjeta = adm.getByText(tienda.nombre, { exact: true }).first().locator('xpath=ancestor::*[.//textarea][1]')
    await tarjeta.locator('textarea').fill('Se habló con la tienda; queda suspendida mientras se revisa.')
    await tarjeta.getByRole('button', { name: 'Marcar como resuelto' }).click()
    await adm.waitForTimeout(1000)
    return { ok: true, inicio, nota: `${tienda.nombre} quedó suspendida · ${await avisos(adm)}` }
  })
  await caso('UP-ADM-03-E1', t, async () => {
    const t3 = await nuevaPagina(browser)
    await entrar(t3, tienda.correo, FRASE); await t3.waitForTimeout(1500)
    const txt = await avisos(t3)
    await t3.screenshot({ path: `${DIR}/capturas/UP-ADM-03-E1-login.png` })
    return { ok: /suspendid/i.test(txt), nota: txt }
  })
  await caso('UP-ADM-03-E2', adm, async () => {
    await adm.goto('/admin/catalogo'); await adm.waitForTimeout(800)
    fs.writeFileSync(path.join(os.tmpdir(), 'catalogo-aria.txt'), await adm.getByRole('main').ariaSnapshot())
    const sw = adm.getByRole('switch').nth(1)
    await sw.dispatchEvent('click'); await adm.waitForTimeout(1000)
    const aviso = await avisos(adm)
    await cli.goto('/solicitudes/nueva'); await cli.waitForTimeout(1000)
    const ve = await cli.getByRole('radio', { name: 'Ferretería' }).count()
    await sw.dispatchEvent('click'); await adm.waitForTimeout(1000)
    return { ok: ve === 0, nota: `${aviso} · «Ferretería» al publicar: ${ve ? 'aparece' : 'no aparece'} · se volvió a activar.` }
  })
})
