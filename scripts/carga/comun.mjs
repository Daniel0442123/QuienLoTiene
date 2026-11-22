// Utilidades de los guiones de carga (Plan Maestro §10.3). Node 22 con fetch; detrás de un proxy usar NODE_USE_ENV_PROXY=1.
import { randomBytes } from 'node:crypto'

export const BASE = process.env.BASE ?? 'https://quienlotiene.tech'
export const API = `${BASE}/api/v1`

export async function json(ruta, { metodo = 'GET', token, cuerpo } = {}) {
  const respuesta = await fetch(`${API}${ruta}`, {
    method: metodo,
    headers: {
      ...(cuerpo ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: cuerpo ? JSON.stringify(cuerpo) : undefined,
  })
  const texto = await respuesta.text()
  if (!respuesta.ok) throw new Error(`${metodo} ${ruta} -> ${respuesta.status} ${texto.slice(0, 200)}`)
  return texto ? JSON.parse(texto) : null
}

export const frase = () => `carga ${randomBytes(12).toString('hex')}`

export async function registrarCliente(correo, fraseContrasena) {
  await json('/auth/registro', {
    metodo: 'POST',
    cuerpo: {
      nombre: 'Cliente de carga',
      correo,
      telefono: '3000000001',
      fraseContrasena,
      aceptaPoliticaDatos: true,
      versionPoliticaDatos: '1.0',
    },
  })
}

export async function iniciarSesion(correo, fraseContrasena) {
  const sesion = await json('/auth/login', { metodo: 'POST', cuerpo: { correo, fraseContrasena } })
  if (!sesion.tokenAcceso) throw new Error(`El inicio de sesión de ${correo} pidió segundo factor`)
  return sesion.tokenAcceso
}

/**
 * Abre /api/v1/eventos y llama a alEvento(tipo, datos) por cada evento. Devuelve { abierta, cerrar }.
 * «abierta» se resuelve con el código HTTP al recibir los encabezados.
 */
export function abrirEventos(token, alEvento = () => {}) {
  const control = new AbortController()
  let resolverAbierta
  const abierta = new Promise((r) => (resolverAbierta = r))
  ;(async () => {
    try {
      const respuesta = await fetch(`${API}/eventos`, {
        headers: { Authorization: `Bearer ${token}`, Accept: 'text/event-stream' },
        signal: control.signal,
      })
      resolverAbierta(respuesta.status)
      const lector = respuesta.body.getReader()
      const decodificador = new TextDecoder()
      let resto = ''
      for (;;) {
        const { value, done } = await lector.read()
        if (done) break
        resto += decodificador.decode(value, { stream: true })
        let corte
        while ((corte = resto.indexOf('\n\n')) >= 0) {
          const bloque = resto.slice(0, corte)
          resto = resto.slice(corte + 2)
          let tipo = 'message'
          const datos = []
          for (const linea of bloque.split('\n')) {
            if (linea.startsWith('event:')) tipo = linea.slice(6).trim()
            else if (linea.startsWith('data:')) datos.push(linea.slice(5).trim())
          }
          alEvento(tipo, datos.join('\n'), Date.now())
        }
      }
      alEvento('__cerrada__', '', Date.now())
    } catch (error) {
      if (error.name !== 'AbortError') {
        resolverAbierta(0)
        alEvento('__error__', String(error), Date.now())
      }
    }
  })()
  return { abierta, cerrar: () => control.abort() }
}

export function percentil(valores, p) {
  const ordenados = [...valores].sort((a, b) => a - b)
  return ordenados[Math.min(ordenados.length - 1, Math.ceil((p / 100) * ordenados.length) - 1)]
}
