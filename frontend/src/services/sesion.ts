import { errorDesdeRespuesta, errorSinConexion } from './errores'

/**
 * Sesión del usuario (DECISIONES-FRONTEND F-06).
 *
 * - El token de acceso (15 min) vive SOLO en memoria: nunca en localStorage ni sessionStorage.
 * - El token de refresco viaja en la cookie HttpOnly qlt_refresco (ruta /api/v1/auth); el JavaScript no la ve.
 * - Al abrir la app se llama a renovarSesion() para recuperar la sesión si la cookie sigue vigente.
 */

export type Rol = 'CLIENTE' | 'TIENDA' | 'ADMIN'

export interface Sesion {
  usuarioId: string
  rol: Rol
  tokenAcceso: string
  fechaExpiracion: string
}

/** Respuesta de /auth/login, /auth/totp y /auth/refresco (SesionResponse del backend). */
interface SesionResponse extends Sesion {
  tipoToken: 'Bearer'
  segundoFactorRequerido: false
}

/** Respuesta de /auth/login cuando el administrador debe escribir el código TOTP. */
export interface SegundoFactorPendiente {
  segundoFactorRequerido: true
  tokenSegundoFactor: string
  fechaExpiracion: string
}

export type RespuestaInicioSesion = SesionResponse | SegundoFactorPendiente

const RUTA_AUTENTICACION = '/api/v1/auth'

let sesionActual: Sesion | null = null
const suscriptores = new Set<() => void>()
let renovacionEnCurso: Promise<Sesion | null> | null = null

function notificarCambio(): void {
  suscriptores.forEach((avisar) => avisar())
}

export function suscribirseASesion(avisar: () => void): () => void {
  suscriptores.add(avisar)
  return () => suscriptores.delete(avisar)
}

export function obtenerSesion(): Sesion | null {
  return sesionActual
}

export function obtenerTokenAcceso(): string | null {
  return sesionActual?.tokenAcceso ?? null
}

export function guardarSesion(respuesta: SesionResponse): Sesion {
  sesionActual = {
    usuarioId: respuesta.usuarioId,
    rol: respuesta.rol,
    tokenAcceso: respuesta.tokenAcceso,
    fechaExpiracion: respuesta.fechaExpiracion,
  }
  notificarCambio()
  return sesionActual
}

export function limpiarSesion(): void {
  sesionActual = null
  notificarCambio()
}

async function publicarEnAutenticacion(ruta: string, cuerpo?: unknown): Promise<Response> {
  try {
    return await fetch(`${RUTA_AUTENTICACION}${ruta}`, {
      method: 'POST',
      credentials: 'same-origin',
      headers: cuerpo ? { 'Content-Type': 'application/json' } : undefined,
      body: cuerpo ? JSON.stringify(cuerpo) : undefined,
    })
  } catch {
    throw errorSinConexion()
  }
}

export async function iniciarSesion(correo: string, fraseContrasena: string): Promise<RespuestaInicioSesion> {
  const respuesta = await publicarEnAutenticacion('/login', { correo, fraseContrasena })
  if (!respuesta.ok) throw await errorDesdeRespuesta(respuesta)
  const datos = (await respuesta.json()) as RespuestaInicioSesion
  if (!datos.segundoFactorRequerido) guardarSesion(datos)
  return datos
}

/**
 * Pide un token de acceso nuevo con la cookie de refresco. Si varias peticiones lo piden a la vez, todas esperan la
 * misma renovación (el backend rota el token de refresco y una segunda renovación simultánea fallaría).
 * Devuelve null si no hay sesión que recuperar (IDE-010).
 */
export function renovarSesion(): Promise<Sesion | null> {
  renovacionEnCurso ??= (async () => {
    try {
      const respuesta = await publicarEnAutenticacion('/refresco')
      if (!respuesta.ok) {
        limpiarSesion()
        return null
      }
      return guardarSesion((await respuesta.json()) as SesionResponse)
    } finally {
      renovacionEnCurso = null
    }
  })()
  return renovacionEnCurso
}

export async function cerrarSesion(): Promise<void> {
  try {
    await publicarEnAutenticacion('/logout')
  } finally {
    limpiarSesion()
  }
}
