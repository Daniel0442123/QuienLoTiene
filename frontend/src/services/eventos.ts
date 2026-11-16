import { EventStreamContentType, fetchEventSource } from '@microsoft/fetch-event-source'
import { obtenerTokenAcceso, renovarSesion } from './sesion'

/**
 * Flujo de eventos en tiempo real (HU-030, DECISIONES-FRONTEND §7.1, F-14). `EventSource` no envía la cabecera
 * `Authorization`, así que se abre con `fetch` mediante `@microsoft/fetch-event-source`:
 * - Una conexión por usuario. Con la app oculta se cierra y al volver se abre de nuevo (lo hace la librería).
 * - Si la conexión se cae, reintenta con espera creciente y envía `Last-Event-ID` para recibir lo perdido.
 * - Con `401` renueva la sesión una vez y vuelve a abrir; si no hay sesión, se detiene.
 * El latido `:latido` del servidor es un comentario: la librería lo ignora.
 */

const RUTA = '/api/v1/eventos'
const ESPERAS_MS = [1_000, 2_000, 5_000, 10_000, 30_000]

class SinSesion extends Error {}
class ConexionCerrada extends Error {}

export interface OpcionesEventos {
  /** `tipo` es el campo `event:` del SSE (por ejemplo `solicitud-nueva`) y `datos` su `data:` en JSON. */
  alEvento: (tipo: string, datos: unknown) => void
  senal: AbortSignal
}

/** Espera de cada reintento: 1 s, 2 s, 5 s, 10 s y luego cada 30 s. */
export function esperaParaReintento(intento: number): number {
  return ESPERAS_MS[Math.min(intento, ESPERAS_MS.length - 1)] ?? 30_000
}

export async function escucharEventos({ alEvento, senal }: OpcionesEventos): Promise<void> {
  let ultimoId: string | undefined
  while (!senal.aborted) {
    const token = obtenerTokenAcceso()
    if (!token) return
    let intento = 0
    try {
      await fetchEventSource(`${globalThis.location?.origin ?? ''}${RUTA}`, {
        headers: { Authorization: `Bearer ${token}`, ...(ultimoId ? { 'Last-Event-ID': ultimoId } : {}) },
        signal: senal,
        // Se busca fetch en cada llamada: así lo ve MSW en las pruebas y en el modo simulado
        fetch: (peticion, opciones) => globalThis.fetch(peticion, opciones),
        async onopen(respuesta) {
          if (respuesta.status === 401) throw new SinSesion()
          const tipo = respuesta.headers.get('Content-Type') ?? ''
          if (!respuesta.ok || !tipo.startsWith(EventStreamContentType)) {
            throw new ConexionCerrada(`El flujo de eventos respondió ${respuesta.status}`)
          }
          intento = 0
        },
        onmessage(mensaje) {
          if (mensaje.id) ultimoId = mensaje.id
          if (!mensaje.event || !mensaje.data) return
          try {
            alEvento(mensaje.event, JSON.parse(mensaje.data))
          } catch {
            // Un evento mal formado no debe cortar la conexión
          }
        },
        onclose() {
          // El servidor cerró (por ejemplo, otra pestaña abrió la conexión del mismo usuario): se reintenta
          throw new ConexionCerrada('El servidor cerró el flujo de eventos')
        },
        onerror(error: unknown) {
          if (error instanceof SinSesion) throw error
          return esperaParaReintento(intento++)
        },
      })
      return
    } catch (error) {
      if (senal.aborted || !(error instanceof SinSesion)) return
      const sesion = await renovarSesion().catch(() => null)
      if (!sesion) return
    }
  }
}
