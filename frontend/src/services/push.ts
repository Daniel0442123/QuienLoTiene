/**
 * Notificaciones push del navegador (HU-031, decisión 9 del contrato del Sprint 3). Aquí solo se habla con el
 * navegador (permiso, PushManager); las llamadas a la API van en features/notificaciones/api.ts.
 *
 * El permiso se pide solo después de un toque de la persona (los navegadores lo exigen y es lo que pide la historia),
 * y nunca si el servidor dice que el push está apagado.
 */

export type EstadoPush =
  /** El navegador no tiene Push API (por ejemplo, Safari fuera de la app instalada). */
  | 'no-soportado'
  /** No hay service worker registrado: pasa en `npm run dev`, donde vite-plugin-pwa no lo registra. */
  | 'sin-service-worker'
  /** La persona bloqueó los avisos de este sitio en el navegador. */
  | 'bloqueado'
  | 'activo'
  | 'inactivo'

export function pushSoportado(): boolean {
  return (
    typeof navigator !== 'undefined' &&
    'serviceWorker' in navigator &&
    typeof window !== 'undefined' &&
    'PushManager' in window &&
    'Notification' in window
  )
}

/** La clave pública VAPID llega en base64url; PushManager.subscribe la quiere en bytes. */
export function claveAplicacion(base64url: string): Uint8Array<ArrayBuffer> {
  const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/')
  const relleno = '='.repeat((4 - (base64.length % 4)) % 4)
  const texto = atob(base64 + relleno)
  const bytes = new Uint8Array(new ArrayBuffer(texto.length))
  for (let i = 0; i < texto.length; i += 1) bytes[i] = texto.charCodeAt(i)
  return bytes
}

async function registroDelServiceWorker(): Promise<ServiceWorkerRegistration | null> {
  return (await navigator.serviceWorker.getRegistration()) ?? null
}

/** Cómo están los avisos en este navegador, y su suscripción si la hay. */
export async function consultarEstadoPush(): Promise<{ estado: EstadoPush; suscripcion: PushSubscription | null }> {
  if (!pushSoportado()) return { estado: 'no-soportado', suscripcion: null }
  if (Notification.permission === 'denied') return { estado: 'bloqueado', suscripcion: null }
  const registro = await registroDelServiceWorker()
  if (!registro) return { estado: 'sin-service-worker', suscripcion: null }
  const suscripcion = await registro.pushManager.getSubscription()
  return { estado: suscripcion && Notification.permission === 'granted' ? 'activo' : 'inactivo', suscripcion }
}

/**
 * Pide el permiso y suscribe este navegador. Devuelve la suscripción, o 'bloqueado' si la persona no dio el permiso.
 * `userVisibleOnly: true`: cada push muestra una notificación (lo exigen Chrome y Firefox).
 */
export async function suscribirEsteNavegador(clavePublica: string): Promise<PushSubscription | 'bloqueado'> {
  const permiso = await Notification.requestPermission()
  if (permiso !== 'granted') return 'bloqueado'
  const registro = await registroDelServiceWorker()
  if (!registro) throw new Error('No hay service worker para recibir los avisos')
  return registro.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey: claveAplicacion(clavePublica) })
}
