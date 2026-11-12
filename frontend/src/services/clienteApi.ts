import createClient, { type Middleware } from 'openapi-fetch'
import type { paths } from './api-tipos'
import type { PathsProvisionales } from './api-tipos-provisionales'
import { errorDesdeRespuesta, errorSinConexion } from './errores'
import { obtenerTokenAcceso, renovarSesion } from './sesion'

/**
 * Cliente HTTP tipado con el contrato OpenAPI (DECISIONES-FRONTEND F-04).
 *
 * - Rutas relativas (/api/v1/...): en desarrollo las atiende el proxy de Vite, en producción Caddy (F-05).
 * - Agrega Authorization: Bearer con el token en memoria.
 * - Si la API responde 401, renueva la sesión una sola vez y repite la petición.
 * - Cualquier otra respuesta fallida se lanza como ErrorApi (ProblemDetail), para que TanStack Query la maneje.
 */

/** Copia de cada petición, para poder repetirla después de renovar la sesión (el cuerpo solo se lee una vez). */
const copiasParaRepetir = new WeakMap<Request, Request>()

const autenticacion: Middleware = {
  onRequest({ request }) {
    const token = obtenerTokenAcceso()
    if (token) request.headers.set('Authorization', `Bearer ${token}`)
    copiasParaRepetir.set(request, request.clone())
    return request
  },
}

const renovacionYErrores: Middleware = {
  async onResponse({ request, response }) {
    if (response.ok) return response

    const copia = copiasParaRepetir.get(request)
    if (response.status === 401 && copia) {
      const sesion = await renovarSesion()
      if (sesion) {
        copia.headers.set('Authorization', `Bearer ${sesion.tokenAcceso}`)
        const respuestaRepetida = await fetch(copia)
        if (respuestaRepetida.ok) return respuestaRepetida
        throw await errorDesdeRespuesta(respuestaRepetida)
      }
    }
    throw await errorDesdeRespuesta(response)
  },
  onError() {
    throw errorSinConexion()
  },
}

// PathsProvisionales se quita cuando api-tipos.ts se genere desde el backend (ver api-tipos-provisionales.ts)
// El origen de la página: las rutas siguen siendo del mismo origen, pero `Request` en Node (pruebas) exige URL absoluta
export const clienteApi = createClient<paths & PathsProvisionales>({
  baseUrl: globalThis.location?.origin ?? '',
  credentials: 'same-origin',
  // Se busca fetch en cada llamada (no al importar): así lo ven las herramientas que lo envuelven, como MSW en las pruebas
  fetch: (peticion) => globalThis.fetch(peticion),
})
clienteApi.use(autenticacion, renovacionYErrores)
