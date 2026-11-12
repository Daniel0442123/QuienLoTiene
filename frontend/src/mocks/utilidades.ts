import { HttpResponse } from 'msw'
import type { ErrorDeCampo } from '@/services/errores'
import { base, type RolSimulado, type UsuarioSimulado } from './datos'

/** Prefijo de los tokens de acceso simulados: `simulado.<usuarioId>`. */
const PREFIJO_TOKEN = 'simulado.'
const QUINCE_MINUTOS_MS = 15 * 60 * 1000

export const TELEFONO_VALIDO = /^\+?[0-9]{7,15}$/
export const CORREO_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

/** Respuesta de error con la forma de ProblemDetail del backend (CODIGOS-DE-ERROR.md). */
export function problema(
  status: number,
  codigo: string,
  title: string,
  opciones: { detail?: string; errores?: ErrorDeCampo[]; cabeceras?: Record<string, string> } = {},
) {
  const idCorrelacion = `simulado-${crypto.randomUUID().slice(0, 8)}`
  return HttpResponse.json(
    {
      type: `https://quienlotiene.app/errores/${codigo.toLowerCase()}`,
      title,
      status,
      detail: opciones.detail,
      codigo,
      idCorrelacion,
      errores: opciones.errores ?? [],
    },
    { status, headers: { 'X-Correlacion-Id': idCorrelacion, ...opciones.cabeceras } },
  )
}

/** Junta los errores de campo; si hay alguno responde GEN-001 como la validación del backend. */
export function validar(reglas: Array<[campo: string, falla: boolean, mensaje: string]>) {
  const errores = reglas.filter(([, falla]) => falla).map(([campo, , mensaje]) => ({ campo, mensaje }))
  return errores.length > 0 ? problema(400, 'GEN-001', 'Datos inválidos', { errores }) : null
}

export function vacio(valor: unknown): boolean {
  return typeof valor !== 'string' || valor.trim() === ''
}

export function tokenPara(usuario: UsuarioSimulado): string {
  return `${PREFIJO_TOKEN}${usuario.id}`
}

/** SesionResponse del backend. */
export function respuestaSesion(usuario: UsuarioSimulado) {
  return HttpResponse.json(
    {
      usuarioId: usuario.id,
      rol: usuario.rol,
      tokenAcceso: tokenPara(usuario),
      tipoToken: 'Bearer',
      fechaExpiracion: new Date(Date.now() + QUINCE_MINUTOS_MS).toISOString(),
      segundoFactorRequerido: false,
    },
    { headers: { 'Cache-Control': 'no-store' } },
  )
}

/**
 * Devuelve el usuario del token o la respuesta de error que daría el backend:
 * 401 GEN-003 sin token válido, 403 GEN-004 si el rol no corresponde.
 */
export function autorizar(request: Request, roles?: RolSimulado[]): UsuarioSimulado | Response {
  const cabecera = request.headers.get('Authorization') ?? ''
  const token = cabecera.startsWith('Bearer ') ? cabecera.slice('Bearer '.length) : ''
  const usuario = token.startsWith(PREFIJO_TOKEN)
    ? base.usuarios.find((u) => u.id === token.slice(PREFIJO_TOKEN.length))
    : undefined
  if (!usuario) return problema(401, 'GEN-003', 'Sesión requerida')
  if (roles && !roles.includes(usuario.rol)) return problema(403, 'GEN-004', 'Sin permiso')
  return usuario
}

export async function leerJson<T>(request: Request): Promise<Partial<T>> {
  try {
    return ((await request.json()) ?? {}) as Partial<T>
  } catch {
    return {}
  }
}

export function ubicacionInvalida(ubicacion: { latitud?: unknown; longitud?: unknown } | undefined): boolean {
  if (!ubicacion) return true
  const { latitud, longitud } = ubicacion
  return (
    typeof latitud !== 'number' ||
    typeof longitud !== 'number' ||
    latitud < -90 ||
    latitud > 90 ||
    longitud < -180 ||
    longitud > 180
  )
}
