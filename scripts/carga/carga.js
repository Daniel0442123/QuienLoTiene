// Prueba de carga con k6 (Plan Maestro §10.3). Lee las cuentas que crea preparar.mjs.
//   ESCENARIO=rendimiento → NF-REN-001: 50 usuarios concurrentes, p95 de la API ≤ 500 ms
//   ESCENARIO=escala      → NF-ESC-020: 100 usuarios concurrentes con tasa de error < 1 % (las 50 conexiones SSE las
//                            abre a la vez sse.mjs conexiones)
// Ejemplo: docker run --rm -i -v "$PWD:/carga" -w /carga -e ESCENARIO=rendimiento grafana/k6:1.4.2 run carga.js
import http from 'k6/http'
import { check, sleep } from 'k6'

const BASE = __ENV.BASE || 'https://quienlotiene.tech'
const ESCENARIO = __ENV.ESCENARIO || 'rendimiento'
const DURACION = __ENV.DURACION || '3m'
const cuentas = JSON.parse(open(__ENV.CUENTAS || 'cuentas.json')).cuentas
const usuarios = ESCENARIO === 'escala' ? 100 : 50

export const options = {
  scenarios: {
    [ESCENARIO]: { executor: 'constant-vus', vus: usuarios, duration: DURACION },
  },
  thresholds: {
    'http_req_duration{tipo:api}': ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
}

// Lo que hace un cliente que abre la app: su cuenta, categorías, historial, pedidos, direcciones y la verificación del
// producto restringido antes de publicar
const lecturas = [
  ['/api/v1/usuarios/me', 'usuario'],
  ['/api/v1/categorias', 'categorias'],
  ['/api/v1/solicitudes/mias', 'historial'],
  ['/api/v1/ordenes', 'pedidos'],
  ['/api/v1/clientes/me/direcciones', 'direcciones'],
  ['/api/v1/catalogo/restriccion?producto=acetaminofen', 'restriccion'],
]

export default function () {
  const cuenta = cuentas[(__VU - 1) % cuentas.length]
  const parametros = (nombre) => ({ headers: { Authorization: `Bearer ${cuenta.token}` }, tags: { tipo: 'api', nombre } })
  const respuestas = http.batch(lecturas.map(([ruta, nombre]) => ['GET', `${BASE}${ruta}`, null, parametros(nombre)]))
  respuestas.forEach((r) => check(r, { 'responde 200': (x) => x.status === 200 }))
  // La PWA: index.html (sin caché) como al abrir la app
  check(http.get(`${BASE}/`, { tags: { tipo: 'pwa', nombre: 'index' } }), { 'PWA 200': (x) => x.status === 200 })
  sleep(1)
}
