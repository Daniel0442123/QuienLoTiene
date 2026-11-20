import { http, HttpResponse } from 'msw'
import { base } from './datos'
import { autorizar, leerJson, problema, validar } from './utilidades'

/**
 * Notificaciones push (HU-031), como GestionarSuscripcionesPush y SuscripcionPush del backend: la clave pública VAPID
 * para PushManager.subscribe y alta o baja de la suscripción de este navegador. Solo se aceptan endpoints HTTPS de los
 * servicios push de Chrome, Firefox, Edge y Safari, con claves del tamaño correcto (NOT-001).
 */

const SERVICIOS_PERMITIDOS = [
  'fcm.googleapis.com',
  'updates.push.services.mozilla.com',
  'push.services.mozilla.com',
  'notify.windows.com',
  'push.apple.com',
]
const BYTES_CLAVE_PUBLICA = 65
const BYTES_SECRETO = 16

/** base64url sin relleno, como lo usan VAPID y PushSubscription.toJSON(). */
function aBase64Url(bytes: number[]): string {
  return btoa(String.fromCharCode(...bytes))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '')
}

function bytesDeBase64Url(valor: unknown): number {
  if (typeof valor !== 'string') return -1
  try {
    return atob(valor.replace(/-/g, '+').replace(/_/g, '/')).length
  } catch {
    return -1
  }
}

/** Una clave pública P-256 sin comprimir de ejemplo (65 bytes que empiezan por 0x04). No sirve para enviar nada. */
export const CLAVE_PUBLICA_SIMULADA = aBase64Url([4, ...Array.from({ length: 64 }, (_, i) => (i * 7 + 3) % 256)])

function endpointConocido(endpoint: unknown): boolean {
  if (typeof endpoint !== 'string') return false
  try {
    const url = new URL(endpoint)
    const host = url.hostname.toLowerCase()
    return (
      url.protocol === 'https:' &&
      url.port === '' &&
      url.username === '' &&
      SERVICIOS_PERMITIDOS.some((servicio) => host === servicio || host.endsWith(`.${servicio}`))
    )
  } catch {
    return false
  }
}

export const manejadoresNotificaciones = [
  // Con habilitado false el frontend no pide el permiso (sin claves VAPID en el servidor)
  http.get('/api/v1/notificaciones/configuracion-push', ({ request }) => {
    const usuario = autorizar(request, ['CLIENTE', 'TIENDA'])
    if (usuario instanceof Response) return usuario
    return HttpResponse.json({ habilitado: true, clavePublica: CLAVE_PUBLICA_SIMULADA })
  }),

  http.post('/api/v1/notificaciones/suscripciones', async ({ request }) => {
    const usuario = autorizar(request, ['CLIENTE', 'TIENDA'])
    if (usuario instanceof Response) return usuario
    const { endpoint, keys } = await leerJson<{ endpoint: string; keys?: { p256dh?: string; auth?: string } }>(request)
    const invalidos = validar([['endpoint', typeof endpoint !== 'string' || endpoint === '', 'Falta el endpoint']])
    if (invalidos) return invalidos
    if (
      !endpointConocido(endpoint) ||
      bytesDeBase64Url(keys?.p256dh) !== BYTES_CLAVE_PUBLICA ||
      bytesDeBase64Url(keys?.auth) !== BYTES_SECRETO
    ) {
      return problema(400, 'NOT-001', 'Suscripción push inválida', {
        detail: 'El endpoint no es de un servicio push de navegador conocido o las claves no son válidas.',
      })
    }
    base.suscripcionesPush = base.suscripcionesPush.filter((s) => s.endpoint !== endpoint)
    base.suscripcionesPush.push({ usuarioId: usuario.id, endpoint: endpoint as string })
    return new HttpResponse(null, { status: 201 })
  }),

  // Responde 204 aunque no exista
  http.delete('/api/v1/notificaciones/suscripciones', ({ request }) => {
    const usuario = autorizar(request, ['CLIENTE', 'TIENDA'])
    if (usuario instanceof Response) return usuario
    const endpoint = new URL(request.url).searchParams.get('endpoint')
    base.suscripcionesPush = base.suscripcionesPush.filter(
      (s) => !(s.endpoint === endpoint && s.usuarioId === usuario.id),
    )
    return new HttpResponse(null, { status: 204 })
  }),
]
