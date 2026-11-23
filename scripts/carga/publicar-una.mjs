// Publica una sola solicitud de prueba con una cuenta de cuentas.json (prueba de push de NF-COM-026).
// Uso: node publicar-una.mjs <lat> <lon> "<producto>" [categoría] [archivo]
import { readFileSync } from 'node:fs'
import { json } from './comun.mjs'

const [lat, lon, producto, categoria = 'Farmacia', archivo = 'cuentas.json'] = process.argv.slice(2)
const { cuentas } = JSON.parse(readFileSync(archivo, 'utf8'))
const cuenta = cuentas[cuentas.length - 1]
const categorias = await json('/categorias')
const cat = categorias.find((c) => c.nombre === categoria)
const solicitud = await json('/solicitudes', {
  metodo: 'POST',
  token: cuenta.token,
  cuerpo: { producto, cantidad: 1, categoriaId: cat.id, modalidad: 'RECOGER', esUrgente: false, ubicacion: { latitud: Number(lat), longitud: Number(lon) } },
})
console.log(new Date().toISOString(), 'publicada', solicitud.id, 'radio', solicitud.radioEnMetros, 'tiendas notificadas', solicitud.tiendasNotificadas)
