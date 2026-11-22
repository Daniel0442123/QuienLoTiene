// Crea N clientes de prueba (dominio prueba.quienlotiene.tech, los borra «limpiar.sh pruebas») y guarda sus tokens en
// el archivo indicado (por defecto cuentas.json, que no se sube a Git). Uso: node preparar.mjs 50 [archivo]
// El token dura 15 minutos: «node preparar.mjs renovar [archivo]» vuelve a iniciar sesión con las mismas cuentas.
import { readFileSync, writeFileSync } from 'node:fs'
import { frase, iniciarSesion, registrarCliente } from './comun.mjs'

if (process.argv[2] === 'renovar') {
  const archivo = process.argv[3] ?? 'cuentas.json'
  const datos = JSON.parse(readFileSync(archivo, 'utf8'))
  for (const cuenta of datos.cuentas) cuenta.token = await iniciarSesion(cuenta.correo, cuenta.frase)
  writeFileSync(archivo, JSON.stringify({ ...datos, renovadas: new Date().toISOString() }, null, 2))
  console.log(`${datos.cuentas.length} sesiones renovadas en ${archivo}`)
  process.exit(0)
}

const cantidad = Number(process.argv[2] ?? 50)
const archivo = process.argv[3] ?? 'cuentas.json'
const corrida = Date.now()
const cuentas = []
for (let i = 0; i < cantidad; i++) {
  const correo = `carga-${corrida}-${i}@prueba.quienlotiene.tech`
  const clave = frase()
  await registrarCliente(correo, clave)
  cuentas.push({ correo, frase: clave, token: await iniciarSesion(correo, clave) })
}
writeFileSync(archivo, JSON.stringify({ creadas: new Date().toISOString(), cuentas }, null, 2))
console.log(`${cuentas.length} clientes listos en ${archivo}`)
