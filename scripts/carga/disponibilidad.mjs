// NF-DIS-019: consulta el sitio y la salud de la API cada segundo durante un despliegue y mide el tiempo sin servicio.
// Uso: node disponibilidad.mjs [segundos]   (Node 22; detrás de un proxy, NODE_USE_ENV_PROXY=1)
const BASE = process.env.BASE ?? 'https://quienlotiene.tech'
const segundos = Number(process.argv[2] ?? 600)
const fin = Date.now() + segundos * 1000
let caidoDesde = null
let totalCaido = 0
const caidas = []
async function sonda(ruta) {
  try {
    const r = await fetch(`${BASE}${ruta}`, { signal: AbortSignal.timeout(2500), cache: 'no-store' })
    return r.status
  } catch {
    return 0
  }
}
while (Date.now() < fin) {
  const inicio = Date.now()
  // index.html de Caddy y una ruta de la API que responde 401 sin sesión: las dos prueban que el servicio contesta
  const [pwa, api] = await Promise.all([sonda('/'), sonda('/api/v1/usuarios/me')])
  const arriba = pwa === 200 && api === 401
  const hora = new Date(inicio).toISOString()
  if (!arriba && caidoDesde === null) {
    caidoDesde = inicio
    console.log(`${hora} SIN SERVICIO (PWA ${pwa}, API ${api})`)
  } else if (arriba && caidoDesde !== null) {
    const duracion = (inicio - caidoDesde) / 1000
    totalCaido += duracion
    caidas.push(duracion)
    console.log(`${hora} vuelve el servicio tras ${duracion.toFixed(1)} s`)
    caidoDesde = null
  }
  await new Promise((r) => setTimeout(r, Math.max(0, 1000 - (Date.now() - inicio))))
}
console.log(`caídas: ${caidas.length}; tiempo total sin servicio: ${totalCaido.toFixed(1)} s; la más larga: ${Math.max(0, ...caidas).toFixed(1)} s`)
