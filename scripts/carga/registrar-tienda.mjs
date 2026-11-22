// Registra la tienda que mide la difusión (NF-REN-002). Queda pendiente: un administrador la aprueba en /admin/tiendas.
// Uso: node registrar-tienda.mjs <lat> <lon> [archivo]  → guarda correo y frase en el archivo (no se sube a Git)
import { readFileSync, writeFileSync } from 'node:fs'
import { API, frase, json } from './comun.mjs'

const [lat, lon] = [Number(process.argv[2]), Number(process.argv[3])]
const archivo = process.argv[4] ?? 'tienda-carga.json'
const categorias = await json('/categorias')
const farmacia = categorias.find((c) => /farmacia/i.test(c.nombre)) ?? categorias[0]
const correo = `carga-tienda-${Date.now()}@prueba.quienlotiene.tech`
const clave = frase()
const png = readFileSync(new URL('../../e2e/pruebas/apoyo/local.png', import.meta.url))
const formulario = new FormData()
formulario.append(
  'datos',
  new Blob([JSON.stringify({ nombreDueno: 'Prueba de carga', correo, telefono: '3000000002', fraseContrasena: clave, aceptaPoliticaDatos: true, versionPoliticaDatos: '1.0', nombreTienda: 'Tienda de prueba de carga', categoriaId: farmacia.id, ubicacion: { latitud: lat, longitud: lon } })], { type: 'application/json' }),
)
formulario.append('fotoLocal', new Blob([png], { type: 'image/png' }), 'local.png')
formulario.append('rut', new Blob([png], { type: 'image/png' }), 'rut.png')
const respuesta = await fetch(`${API}/tiendas/registro`, { method: 'POST', body: formulario })
if (!respuesta.ok) throw new Error(`registro ${respuesta.status} ${await respuesta.text()}`)
writeFileSync(archivo, JSON.stringify({ correo, frase: clave, lat, lon }, null, 2))
console.log(`Tienda de prueba de carga registrada (${farmacia.nombre}) en ${lat}, ${lon}; pendiente de aprobación`)
