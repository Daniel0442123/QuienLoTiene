// Clientes de tiempo real (SSE) para el Plan Maestro §10.3. Uso (Node 22; detrás de un proxy, NODE_USE_ENV_PROXY=1):
//   node sse.mjs conexiones [segundos] [archivo]  → NF-ESC-020: abre 50 conexiones a /api/v1/eventos y las mantiene
//   node sse.mjs difusion [muestras]              → NF-REN-002: tiempo desde publicar una solicitud hasta que la
//                                                   tienda recibe «solicitud-nueva». Necesita TIENDA_CORREO,
//                                                   TIENDA_FRASE, TIENDA_LAT y TIENDA_LON de una tienda aprobada.
import { readFileSync } from 'node:fs'
import { abrirEventos, iniciarSesion, json, percentil } from './comun.mjs'

const modo = process.argv[2]

if (modo === 'conexiones') {
  const segundos = Number(process.argv[3] ?? 200)
  const { cuentas } = JSON.parse(readFileSync(process.argv[4] ?? 'cuentas.json', 'utf8'))
  const estado = cuentas.slice(0, 50).map(() => ({ codigo: null, eventos: 0, error: null, cerrada: false }))
  const conexiones = cuentas.slice(0, 50).map((cuenta, i) =>
    abrirEventos(cuenta.token, (tipo, datos) => {
      if (tipo === '__error__') (estado[i].error = datos), console.log(`${new Date().toISOString()} conexión ${i}: error ${datos}`)
      else if (tipo === '__cerrada__') (estado[i].cerrada = true), console.log(`${new Date().toISOString()} conexión ${i}: el servidor la cerró`)
      else estado[i].eventos++
    }),
  )
  const codigos = await Promise.all(conexiones.map((c) => c.abierta))
  codigos.forEach((c, i) => (estado[i].codigo = c))
  console.log(`${new Date().toISOString()} abiertas: ${codigos.filter((c) => c === 200).length} de ${codigos.length}`)
  await new Promise((r) => setTimeout(r, segundos * 1000))
  const vivas = estado.filter((e) => e.codigo === 200 && !e.cerrada && !e.error).length
  console.log(`${new Date().toISOString()} tras ${segundos} s siguen abiertas: ${vivas} de ${estado.length}`)
  console.log(`errores: ${estado.filter((e) => e.error || e.cerrada || e.codigo !== 200).length} (${((estado.filter((e) => e.error || e.cerrada || e.codigo !== 200).length / estado.length) * 100).toFixed(1)} %)`)
  console.log(`eventos recibidos (incluye latidos): ${estado.reduce((s, e) => s + e.eventos, 0)}`)
  conexiones.forEach((c) => c.cerrar())
  process.exit(vivas === estado.length ? 0 : 1)
}

if (modo === 'difusion') {
  const muestras = Number(process.argv[3] ?? 20)
  const { cuentas } = JSON.parse(readFileSync(process.argv[4] ?? 'cuentas.json', 'utf8'))
  const lat = Number(process.env.TIENDA_LAT)
  const lon = Number(process.env.TIENDA_LON)
  const tokenTienda = await iniciarSesion(process.env.TIENDA_CORREO, process.env.TIENDA_FRASE)
  await json('/tiendas/me/apertura', { metodo: 'PATCH', token: tokenTienda, cuerpo: { abierta: true } })
  const categorias = await json('/categorias', { token: cuentas[0].token })
  const tienda = await json('/tiendas/me', { token: tokenTienda })
  const categoria = categorias.find((c) => c.id === tienda.categoriaId) ?? categorias[0]

  const llegadas = new Map()
  const eventos = abrirEventos(tokenTienda, (tipo, datos, cuando) => {
    if (tipo !== 'solicitud-nueva') return
    try {
      llegadas.set(JSON.parse(datos).solicitudId, cuando)
    } catch {}
  })
  if ((await eventos.abierta) !== 200) throw new Error('La tienda no pudo abrir /eventos')
  await new Promise((r) => setTimeout(r, 1000))

  const tiempos = []
  for (let i = 0; i < muestras; i++) {
    // 10 solicitudes por hora por cliente (RNF-009): se reparte entre las cuentas
    const cuenta = cuentas[Math.floor(i / 5) % cuentas.length]
    const inicio = Date.now()
    const solicitud = await json('/solicitudes', {
      metodo: 'POST',
      token: cuenta.token,
      cuerpo: { producto: `Prueba de difusión ${i + 1}`, cantidad: 1, categoriaId: categoria.id, modalidad: 'RECOGER', esUrgente: false, ubicacion: { latitud: lat, longitud: lon } },
    })
    const limite = Date.now() + 15_000
    while (!llegadas.has(solicitud.id) && Date.now() < limite) await new Promise((r) => setTimeout(r, 20))
    const llegada = llegadas.get(solicitud.id)
    tiempos.push(llegada ? llegada - inicio : Infinity)
    console.log(`muestra ${i + 1}: ${llegada ? `${llegada - inicio} ms` : 'no llegó en 15 s'}`)
    await new Promise((r) => setTimeout(r, 1500))
  }
  eventos.cerrar()
  const llegaron = tiempos.filter(Number.isFinite)
  console.log(`llegaron ${llegaron.length} de ${muestras}; p50 ${percentil(llegaron, 50)} ms, p95 ${percentil(tiempos, 95)} ms, máximo ${Math.max(...tiempos)} ms`)
  process.exit(percentil(tiempos, 95) <= 3000 ? 0 : 1)
}

console.error('Uso: node sse.mjs conexiones [segundos] [archivo] | node sse.mjs difusion [muestras] [archivo]')
process.exit(2)
