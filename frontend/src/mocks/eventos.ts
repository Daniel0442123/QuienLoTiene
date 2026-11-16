import { http, HttpResponse } from 'msw'
import { autorizar } from './utilidades'

/**
 * Flujo SSE simulado (HU-030), como `/api/v1/eventos` del contrato: una conexión por usuario (la nueva cierra la
 * anterior), eventos con `id`, `event` y `data` en JSON, y reenvío de lo perdido en 5 minutos con `Last-Event-ID`.
 * Los manejadores llaman a `emitirEvento` en el mismo momento en que el backend lo haría.
 */

const CINCO_MINUTOS_MS = 5 * 60 * 1000
const codificador = new TextEncoder()

interface EventoGuardado {
  id: number
  usuarioId: string
  tipo: string
  datos: unknown
  fecha: number
}

const conexiones = new Map<string, ReadableStreamDefaultController<Uint8Array>>()
let historial: EventoGuardado[] = []
let siguienteId = 1

function enviar(controlador: ReadableStreamDefaultController<Uint8Array>, texto: string): boolean {
  try {
    controlador.enqueue(codificador.encode(texto))
    return true
  } catch {
    return false
  }
}

const formatear = ({ id, tipo, datos }: EventoGuardado) =>
  `id: ${id}\nevent: ${tipo}\ndata: ${JSON.stringify(datos)}\n\n`

/** Envía el evento a cada usuario con la conexión abierta y lo guarda 5 minutos para reenviarlo. */
export function emitirEvento(usuarioIds: Iterable<string>, tipo: string, datos: unknown): void {
  const ahora = Date.now()
  historial = historial.filter((evento) => ahora - evento.fecha < CINCO_MINUTOS_MS)
  for (const usuarioId of new Set(usuarioIds)) {
    const evento = { id: siguienteId++, usuarioId, tipo, datos, fecha: ahora }
    historial.push(evento)
    const controlador = conexiones.get(usuarioId)
    if (controlador && !enviar(controlador, formatear(evento))) conexiones.delete(usuarioId)
  }
}

/** Las pruebas cierran las conexiones y borran el historial después de cada caso. */
export function reiniciarEventosSimulados(): void {
  for (const controlador of conexiones.values()) {
    try {
      controlador.close()
    } catch {
      // Ya estaba cerrada
    }
  }
  conexiones.clear()
  historial = []
}

export const manejadoresEventos = [
  http.get('/api/v1/eventos', ({ request }) => {
    const usuario = autorizar(request)
    if (usuario instanceof Response) return usuario
    // Sin Last-Event-ID es una conexión nueva: no se reenvía nada; con él, lo perdido de los últimos 5 minutos
    const cabeceraUltimo = request.headers.get('Last-Event-ID')
    const ultimoRecibido = cabeceraUltimo === null ? Number.POSITIVE_INFINITY : Number(cabeceraUltimo)

    let propio: ReadableStreamDefaultController<Uint8Array> | null = null
    const soltar = () => {
      if (propio && conexiones.get(usuario.id) === propio) conexiones.delete(usuario.id)
    }
    const flujo = new ReadableStream<Uint8Array>({
      start(controlador) {
        propio = controlador
        // Una conexión por usuario: la anterior se cierra
        const anterior = conexiones.get(usuario.id)
        if (anterior) {
          try {
            anterior.close()
          } catch {
            // Ya estaba cerrada
          }
        }
        conexiones.set(usuario.id, controlador)
        enviar(controlador, ':conectado\n\n')
        for (const evento of historial) {
          if (evento.usuarioId === usuario.id && evento.id > ultimoRecibido) enviar(controlador, formatear(evento))
        }
        request.signal.addEventListener('abort', () => {
          soltar()
          try {
            controlador.close()
          } catch {
            // Ya estaba cerrada
          }
        })
      },
      cancel: soltar,
    })
    return new HttpResponse(flujo, {
      headers: { 'Content-Type': 'text/event-stream', 'Cache-Control': 'no-store' },
    })
  }),
]
