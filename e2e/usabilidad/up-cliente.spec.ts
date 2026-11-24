import { test, expect } from '@playwright/test'
import { caso, avisos } from './registro'
import {
  FRASE, correoUnico, entrar, nuevaPagina, prepararTiendaAbierta, configurarDomicilioYPago, guardarDireccion,
  publicarSolicitud, responderSiLoTengo, sesionAdmin,
} from '../pruebas/apoyo/actores'

const MOVIL = { viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true }
async function celular(browser: any) { return (await browser.newContext(MOVIL)).newPage() }

test('UP · cliente: ruta feliz y excepciones', async ({ browser }) => {
  const c = await celular(browser)
  const correo = correoUnico('up-cliente')

  // ---------- UP-CLI-01 ----------
  await caso('UP-CLI-01', c, async () => {
    const inicio = Date.now()
    await c.goto('/')
    await c.getByRole('link', { name: /Crear una cuenta/ }).first().click()
    await c.getByLabel('Nombre').fill('Laura Prueba')
    await c.getByLabel('Correo').fill(correo)
    await c.getByLabel('Celular').fill('3109876543')
    await c.getByLabel('Frase de contraseña').fill(FRASE)
    await c.getByRole('checkbox', { name: /Autorizo el tratamiento/ }).check()
    await c.getByRole('button', { name: 'Crear cuenta' }).click()
    await expect(c.getByRole('main').getByText(/^Cuenta creada./)).toBeVisible()
    return { ok: true, inicio, toques: 7, nota: await avisos(c) }
  })
  const otra = await celular(browser)
  await caso('UP-CLI-01-E1', otra, async () => {
    await otra.goto('/crear-cuenta')
    await otra.getByLabel('Nombre').fill('Laura Repetida')
    await otra.getByLabel('Correo').fill(correo)
    await otra.getByLabel('Celular').fill('3109876543')
    await otra.getByLabel('Frase de contraseña').fill(FRASE)
    await otra.getByRole('checkbox', { name: /Autorizo el tratamiento/ }).check()
    await otra.getByRole('button', { name: 'Crear cuenta' }).click()
    await otra.waitForTimeout(1500)
    const txt = await avisos(otra)
    const conserva = (await otra.getByLabel('Nombre').inputValue()) === 'Laura Repetida'
    return { ok: /(ya|registrad|uso|existe)/i.test(txt) && conserva, nota: `${txt} · datos conservados: ${conserva ? 'sí' : 'no'}` }
  })
  await caso('UP-CLI-01-E2', otra, async () => {
    await otra.goto('/crear-cuenta')
    await otra.getByLabel('Nombre').fill('Pedro Corto')
    await otra.getByLabel('Correo').fill(correoUnico('up-corta'))
    await otra.getByLabel('Celular').fill('3109876543')
    await otra.getByLabel('Frase de contraseña').fill('corta1234')
    await otra.getByRole('checkbox', { name: /Autorizo el tratamiento/ }).check()
    await otra.getByRole('button', { name: 'Crear cuenta' }).click()
    await otra.waitForTimeout(800)
    const txt = await avisos(otra)
    return { ok: /15/.test(txt) && !/Cuenta creada/.test(txt), nota: txt }
  })
  await caso('UP-CLI-01-E3', otra, async () => {
    await otra.goto('/crear-cuenta')
    await otra.getByLabel('Nombre').fill('Sin Casilla')
    await otra.getByLabel('Correo').fill(correoUnico('up-sincasilla'))
    await otra.getByLabel('Celular').fill('3109876543')
    await otra.getByLabel('Frase de contraseña').fill(FRASE)
    await otra.getByRole('button', { name: 'Crear cuenta' }).click()
    await otra.waitForTimeout(800)
    const txt = await avisos(otra)
    return { ok: /(autoriz|política|datos)/i.test(txt) && !/Cuenta creada/.test(txt), nota: txt }
  })
  await caso('UP-CLI-01-E4', otra, async () => {
    await entrar(otra, correo, 'una frase equivocada pero larga')
    await otra.waitForTimeout(1200)
    const txt = await avisos(otra)
    return { ok: /incorrect|no coincide|no son correctos/i.test(txt) && !/frase.*incorrecta$|correo no existe/i.test(txt), nota: txt }
  })

  // entrar para lo demás
  await entrar(c, correo, FRASE)
  await c.waitForURL((u) => !u.pathname.startsWith('/entrar'))

  // ---------- UP-CLI-02 ----------
  await caso('UP-CLI-02', c, async () => {
    const inicio = Date.now()
    await guardarDireccion(c)
    return { ok: true, inicio, nota: await avisos(c) }
  })
  await caso('UP-CLI-02-E1', c, async () => {
    await c.goto('/cuenta')
    await c.getByRole('button', { name: 'Agregar dirección' }).click()
    await c.getByLabel('Nombre de la dirección').fill('Oficina')
    await c.getByRole('textbox', { name: 'Celular de contacto' }).fill('3109876543')
    await c.getByRole('button', { name: 'Guardar dirección' }).click()
    await c.waitForTimeout(800)
    const txt = await avisos(c)
    const conserva = (await c.getByLabel('Nombre de la dirección').inputValue()) === 'Oficina'
    return { ok: /(dirección|ubicación|mapa)/i.test(txt) && conserva, nota: `${txt} · datos conservados: ${conserva ? 'sí' : 'no'}` }
  })

  // Tiendas que responden
  const t1 = await nuevaPagina(browser)
  const tienda = await prepararTiendaAbierta(browser, t1)
  await configurarDomicilioYPago(t1)

  // ---------- UP-CLI-03 ----------
  const producto = `Acetaminofén 500 mg UP ${Date.now() % 10000}`
  await caso('UP-CLI-03', c, async () => {
    const inicio = Date.now()
    await publicarSolicitud(c, producto, { domicilio: true, direccionGuardada: true })
    await expect(c.getByText(/Esperando respuestas/).first()).toBeVisible()
    return { ok: (Date.now() - inicio) / 1000 <= 60, inicio, nota: await avisos(c) }
  })
  const c2 = await celular(browser)
  const correo2 = correoUnico('up-cliente2')
  await c2.goto('/crear-cuenta')
  await c2.getByLabel('Nombre').fill('Cliente Dos'); await c2.getByLabel('Correo').fill(correo2)
  await c2.getByLabel('Celular').fill('3109876543'); await c2.getByLabel('Frase de contraseña').fill(FRASE)
  await c2.getByRole('checkbox', { name: /Autorizo el tratamiento/ }).check(); await c2.getByRole('button', { name: 'Crear cuenta' }).click()
  await expect(c2.getByRole('main').getByText(/^Cuenta creada./)).toBeVisible()
  await entrar(c2, correo2, FRASE); await c2.waitForURL((u) => !u.pathname.startsWith('/entrar'))
  await caso('UP-CLI-03-E1', c2, async () => {
    await c2.goto('/solicitudes/nueva')
    await c2.getByRole('button', { name: 'Preguntar' }).click()
    await c2.waitForTimeout(800)
    const txt = await avisos(c2)
    const faltan = ['qué estás buscando', 'farmacia o ferretería', 'cerca de dónde'].filter((f) => txt.toLowerCase().includes(f))
    return { ok: faltan.length >= 2, nota: `${txt} · datos que señala a la vez: ${faltan.length}` }
  })
  await caso('UP-CLI-03-E2', c2, async () => {
    await c2.goto('/solicitudes/nueva')
    await c2.getByRole('textbox', { name: '¿Qué estás buscando?' }).fill('Amoxicilina 500 mg')
    await c2.getByRole('radio', { name: 'Farmacia' }).check()
    await c2.waitForTimeout(1500)
    const txt = (await c2.getByRole('main').innerText()).replace(/\s+/g, ' ')
    const aviso = /fórmula/i.test(txt)
    const domicilio = c2.getByRole('radio', { name: 'Domicilio' })
    const domDisabled = (await domicilio.count()) === 0 || (await domicilio.isDisabled())
    const m = txt.match(/[^.]*fórmula[^.]*\./i)
    return { ok: aviso && domDisabled, nota: `Aviso: ${m ? m[0].trim() : 'no aparece'} · Domicilio deshabilitado: ${domDisabled ? 'sí' : 'no'}` }
  })

  // ---------- UP-CLI-04 (con E3 antes de elegir) ----------
  await caso('UP-CLI-04-E3', c, async () => {
    const n = await c.getByRole('link', { name: /WhatsApp/ }).count()
    return { ok: n === 0, nota: `Botones de WhatsApp antes de elegir: ${n}` }
  })
  await responderSiLoTengo(t1, producto, 8000)
  await caso('UP-CLI-03-E3', c, async () => {
    await expect(c.getByRole('link', { name: `Elegir ${tienda.nombre}` })).toBeVisible()
    await c.reload()
    await expect(c.getByRole('link', { name: `Elegir ${tienda.nombre}` })).toBeVisible()
    return { ok: true, nota: 'Después de recargar siguen la solicitud y la respuesta de la tienda.' }
  })
  await caso('UP-CLI-04', c, async () => {
    const inicio = Date.now()
    await c.getByRole('link', { name: `Elegir ${tienda.nombre}` }).click()
    await expect(c.getByRole('heading', { name: 'Confirmar pedido', level: 1 })).toBeVisible()
    await c.getByRole('radio', { name: 'Domicilio · $ 3.000' }).check()
    await c.getByRole('radio', { name: 'Efectivo' }).check()
    await c.getByRole('button', { name: 'Confirmar pedido · $ 11.000' }).click()
    await c.waitForURL(/\/pedidos\/[0-9a-f-]{36}$/)
    await expect(c.getByText(/^Confirmado · .*\(estado actual\)$/)).toBeVisible()
    return { ok: true, inicio, toques: 4, nota: 'Pedido Confirmado; desglose $ 8.000 + $ 3.000 = $ 11.000.' }
  })
  const pedidoUrl = c.url()
  await caso('UP-CLI-04-E1', c, async () => {
    // la tienda solo acepta efectivo (configurarDomicilioYPago); se revisa que el formulario no ofrezca otros
    await c.goBack()
    await c.waitForTimeout(1000)
    const radios = await c.getByRole('radio').allInnerTexts().catch(() => [])
    const main = (await c.getByRole('main').innerText()).replace(/\s+/g, ' ')
    const ofreceOtros = /(Transferencia|Datáfono)/.test(main)
    return { ok: !ofreceOtros, nota: `Medios ofrecidos con la tienda que solo acepta efectivo: ${ofreceOtros ? 'incluye otros' : 'solo efectivo'} · ${main.slice(0, 160)}` }
  })
  await c.goto(pedidoUrl)

  // ---------- UP-CLI-05 ----------
  await caso('UP-CLI-05', c, async () => {
    const inicio = Date.now()
    await t1.goto('/tienda/pedidos'); await t1.getByRole('link', { name: new RegExp(producto) }).click()
    await t1.getByRole('button', { name: 'Empezar a preparar' }).click()
    await expect(c.getByText(/^Preparando · .*\(estado actual\)$/)).toBeVisible()
    const wa = await c.getByRole('link', { name: /WhatsApp/ }).count()
    await c.getByRole('link', { name: /chat/i }).first().click()
    await c.getByRole('textbox').last().fill('¿A qué hora llega?')
    await c.getByRole('button', { name: /Enviar/ }).click()
    await expect(c.getByText('¿A qué hora llega?').first()).toBeVisible()
    return { ok: wa > 0, inicio, nota: `Estado Preparando llegó sin recargar; botón de WhatsApp: ${wa > 0 ? 'sí' : 'no'}; mensaje enviado.` }
  })

  // ---------- UP-CLI-06 (cancelar en otro pedido) y E1 en este ----------
  await c.goto(pedidoUrl)
  await t1.getByRole('button', { name: 'Salió a domicilio' }).click()
  await expect(c.getByText(/^En camino · .*\(estado actual\)$/)).toBeVisible()
  await caso('UP-CLI-06-E1', c, async () => {
    const cancelar = c.getByRole('button', { name: /Cancelar pedido/ })
    const n = await cancelar.count()
    const main = (await c.getByRole('main').innerText()).replace(/\s+/g, ' ')
    const sugiere = /report/i.test(main)
    return { ok: n === 0 && sugiere, nota: `Botón cancelar visible en «En camino»: ${n ? 'sí' : 'no'} · sugiere reportar: ${sugiere ? 'sí' : 'no'}` }
  })

  // ---------- UP-CLI-07 ----------
  await t1.getByRole('button', { name: 'Marcar como entregado' }).click()
  await expect(c.getByText(/^Entregado · .*\(estado actual\)$/)).toBeVisible()
  await caso('UP-CLI-07', c, async () => {
    const inicio = Date.now()
    await c.getByRole('button', { name: 'Recibí mi pedido' }).click()
    await expect(c.getByText(/^Recibido · .*\(estado actual\)$/)).toBeVisible()
    await c.getByRole('link', { name: new RegExp(`Calificar a ${tienda.nombre}`) }).click()
    await c.getByRole('radio', { name: '4 estrellas' }).check({ force: true })
    await c.getByLabel('Comentario (opcional)').fill('Llegó rápido y bien empacado.')
    await c.getByRole('button', { name: 'Publicar calificación' }).click()
    await c.waitForTimeout(1200)
    return { ok: true, inicio, nota: await avisos(c) }
  })
  await caso('UP-CLI-05-E1', c, async () => {
    await c.goto(pedidoUrl + '/chat')
    await c.waitForTimeout(1000)
    const main = (await c.getByRole('main').innerText()).replace(/\s+/g, ' ')
    const caja = c.getByRole('textbox')
    const deshab = (await caja.count()) === 0 || (await caja.last().isDisabled())
    return { ok: deshab, nota: `Chat en solo lectura: ${deshab ? 'sí' : 'no'} · ${main.match(/[^.]*(lectura|cerr|termin)[^.]*\./i)?.[0] ?? ''}` }
  })
  await caso('UP-CLI-07-E1', c, async () => {
    await c.goto(pedidoUrl)
    await c.waitForTimeout(800)
    const n = await c.getByRole('link', { name: new RegExp(`Calificar a ${tienda.nombre}`) }).count()
    let nota = `Enlace para calificar otra vez: ${n ? 'sí' : 'no'}`
    if (n === 0) {
      await c.goto(pedidoUrl + '/calificar'); await c.waitForTimeout(1000)
      nota += ` · al abrir /calificar: ${(await c.getByRole('main').innerText()).replace(/\s+/g, ' ').slice(0, 160)}`
    }
    return { ok: n === 0, nota }
  })
  await caso('UP-CLI-07-E2', c, async () => {
    await c.goto(pedidoUrl + '/reportar'); await c.waitForTimeout(800)
    await c.getByRole('radio', { name: /Otro problema/ }).check({ force: true })
    await c.getByRole('button', { name: /Enviar|Reportar/ }).last().click()
    await c.waitForTimeout(800)
    const txt = await avisos(c)
    return { ok: /cuéntanos qué pasó/i.test(txt) && !/Enviamos tu reporte/.test(txt), nota: txt }
  })

  // ---------- UP-CLI-06 ruta feliz: otro pedido que se cancela ----------
  const producto2 = `Gasas estériles UP ${Date.now() % 10000}`
  await publicarSolicitud(c, producto2, { domicilio: true, direccionGuardada: true })
  await responderSiLoTengo(t1, producto2, 5000)
  await c.getByRole('link', { name: `Elegir ${tienda.nombre}` }).click()
  await c.getByRole('radio', { name: 'Domicilio · $ 3.000' }).check()
  await c.getByRole('radio', { name: 'Efectivo' }).check()
  await c.getByRole('button', { name: /Confirmar pedido/ }).click()
  await c.waitForURL(/\/pedidos\/[0-9a-f-]{36}$/)
  await caso('UP-CLI-06-E2', c, async () => {
    await c.getByRole('button', { name: /Cancelar pedido/ }).first().click()
    await c.getByRole('button', { name: 'Sí, cancelar el pedido' }).click()
    await c.waitForTimeout(800)
    const txt = await avisos(c)
    return { ok: /por qué cancelas/i.test(txt) && !/Cancelado/.test(txt), nota: txt }
  })
  await caso('UP-CLI-06', c, async () => {
    const inicio = Date.now()
    await c.getByRole('textbox', { name: /por qué cancelas/i }).fill('Ya lo conseguí en otra parte.')
    await c.getByRole('button', { name: 'Sí, cancelar el pedido' }).click()
    await expect(c.getByText(/Cancelado/).first()).toBeVisible()
    return { ok: true, inicio, nota: await avisos(c) }
  })
})
