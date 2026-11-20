import { http, HttpResponse } from 'msw'
import { base, type MensajeSimulado, type UsuarioSimulado } from './datos'
import { emitirEvento } from './eventos'
import { actualizarReservas, ordenDe } from './manejadores-ordenes'
import { duenoDe, ordenActiva } from './reglas-sprint-2'
import { autorizar, leerJson, problema } from './utilidades'

/**
 * Mini chat de la orden (HU-033), como ConversarEnOrden del backend: solo los dos participantes; se escribe mientras la
 * orden está activa (MEN-002 si no) y la otra parte recibe el evento `mensaje-recibido` con solo los ids.
 */

const LARGO_MAXIMO_MENSAJE = 1000

function aMensajeVista(mensaje: MensajeSimulado, usuario: UsuarioSimulado) {
  return {
    id: mensaje.id,
    autor: mensaje.autor,
    esMio: mensaje.autorId === usuario.id,
    texto: mensaje.texto,
    fecha: mensaje.fecha,
  }
}

export const manejadoresMensajes = [
  // Mensajes del más antiguo al más reciente y si el chat está en solo lectura (orden en un estado final)
  http.get('/api/v1/ordenes/:ordenId/mensajes', ({ request, params }) => {
    const usuario = autorizar(request, ['CLIENTE', 'TIENDA'])
    if (usuario instanceof Response) return usuario
    actualizarReservas()
    const orden = ordenDe(params.ordenId, usuario)
    if (orden instanceof Response) return orden
    return HttpResponse.json({
      ordenId: orden.id,
      soloLectura: !ordenActiva(orden),
      mensajes: base.mensajes
        .filter((m) => m.ordenId === orden.id)
        .sort((a, b) => Date.parse(a.fecha) - Date.parse(b.fecha))
        .map((m) => aMensajeVista(m, usuario)),
    })
  }),

  http.post('/api/v1/ordenes/:ordenId/mensajes', async ({ request, params }) => {
    const usuario = autorizar(request, ['CLIENTE', 'TIENDA'])
    if (usuario instanceof Response) return usuario
    actualizarReservas()
    const orden = ordenDe(params.ordenId, usuario)
    if (orden instanceof Response) return orden
    const { texto } = await leerJson<{ texto: string }>(request)
    const limpio = typeof texto === 'string' ? texto.trim() : ''
    if (limpio === '' || limpio.length > LARGO_MAXIMO_MENSAJE) {
      return problema(400, 'MEN-003', 'Mensaje inválido', {
        detail: 'El mensaje no puede estar vacío ni pasar de 1000 caracteres.',
      })
    }
    // CP-MEN-033-02: con la orden en un estado final el chat queda de solo lectura
    if (!ordenActiva(orden)) {
      return problema(409, 'MEN-002', 'Chat cerrado', { detail: 'La orden ya terminó: el chat es de solo lectura.' })
    }
    const mensaje: MensajeSimulado = {
      id: crypto.randomUUID(),
      ordenId: orden.id,
      autor: usuario.rol === 'CLIENTE' ? 'CLIENTE' : 'TIENDA',
      autorId: usuario.id,
      texto: limpio,
      fecha: new Date().toISOString(),
    }
    base.mensajes.push(mensaje)
    const otraParte = usuario.rol === 'CLIENTE' ? duenoDe(orden.tiendaId) : [orden.clienteId]
    emitirEvento(otraParte, 'mensaje-recibido', { ordenId: orden.id, mensajeId: mensaje.id })
    return HttpResponse.json(aMensajeVista(mensaje, usuario), { status: 201 })
  }),
]
