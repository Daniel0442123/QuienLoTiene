import { http, HttpResponse } from 'msw'
import {
  base,
  type EstadoOrdenSimulado,
  type MedioPagoSimulado,
  type OrdenSimulada,
  type UsuarioSimulado,
} from './datos'
import { emitirEvento } from './eventos'
import {
  actualizarVencimientos,
  costoDomicilio,
  duenoDe,
  duenosAlcanzados,
  ordenActiva,
  siguienteEstadoValido,
} from './reglas-sprint-2'
import { autorizar, leerJson, problema, validar, vacio } from './utilidades'

/**
 * Órdenes (HU-024, HU-027 y HU-028), contacto por WhatsApp (HU-032), cancelar (HU-029) y reserva para recoger
 * (HU-025), con la forma de /v3/api-docs y las reglas de Orden.java del backend.
 */

const MEDIOS_PAGO: MedioPagoSimulado[] = ['EFECTIVO', 'TRANSFERENCIA', 'DATAFONO']
const ESTADOS_DE_LA_TIENDA: EstadoOrdenSimulado[] = ['PREPARANDO', 'EN_CAMINO', 'LISTO_PARA_RECOGER', 'ENTREGADO']
const MAXIMO_ORDENES = 50
/** HU-025, SC-019: 30 minutos fijos desde que la tienda marca LISTO_PARA_RECOGER (quienlotiene.ordenes.minutos-reserva). */
const MINUTOS_RESERVA = 30

interface CrearOrdenRequest {
  respuestaId: string
  modalidad: 'RECOGER' | 'DOMICILIO'
  direccionId: string | null
  medioPago: MedioPagoSimulado
}

/** ¿El usuario es el cliente o el dueño de la tienda de la orden? */
export function esParteDeLaOrden(orden: OrdenSimulada, usuario: UsuarioSimulado): boolean {
  if (usuario.rol === 'CLIENTE') return orden.clienteId === usuario.id
  return base.tiendas.some((t) => t.id === orden.tiendaId && t.duenoId === usuario.id)
}

/** La orden si el usuario es parte; si no, 404 ORD-001 sin datos (CP-ORD-024-03). */
export function ordenDe(id: unknown, usuario: UsuarioSimulado): OrdenSimulada | Response {
  const orden = base.ordenes.find((o) => o.id === id)
  return orden && esParteDeLaOrden(orden, usuario) ? orden : problema(404, 'ORD-001', 'Orden no encontrada')
}

/** OrdenResponse. La tienda ve los datos del cliente solo con la orden activa; el cliente recibe `null`. */
function aOrdenResponse(orden: OrdenSimulada, usuario: UsuarioSimulado) {
  const tienda = base.tiendas.find((t) => t.id === orden.tiendaId)
  const cliente = base.usuarios.find((u) => u.id === orden.clienteId)
  const direccion = base.direcciones.find((d) => d.id === orden.direccionId)
  const subtotal = orden.precioUnitarioEnPesos * orden.cantidad
  const verCliente = usuario.rol === 'TIENDA' && ordenActiva(orden) && cliente
  return {
    id: orden.id,
    solicitudId: orden.solicitudId,
    respuestaId: orden.respuestaId,
    producto: orden.producto,
    cantidad: orden.cantidad,
    precioUnitarioEnPesos: orden.precioUnitarioEnPesos,
    subtotalEnPesos: subtotal,
    costoDomicilioEnPesos: orden.costoDomicilioEnPesos,
    totalEnPesos: subtotal + orden.costoDomicilioEnPesos,
    modalidad: orden.modalidad,
    medioPago: orden.medioPago,
    estado: orden.estado,
    esActiva: ordenActiva(orden),
    fechaCreacion: orden.fechaCreacion,
    tienda: { id: orden.tiendaId, nombre: tienda?.nombre ?? 'Tienda' },
    cliente: verCliente
      ? {
          nombre: cliente.nombre,
          telefono: telefonoDelCliente(orden),
          direccion:
            orden.modalidad === 'DOMICILIO' && direccion
              ? {
                  nombre: direccion.nombre,
                  direccionEscrita: direccion.direccionEscrita,
                  indicaciones: direccion.indicaciones,
                  ubicacion: { latitud: direccion.latitud, longitud: direccion.longitud },
                }
              : null,
        }
      : null,
    historial: orden.historial,
    reservaVence: orden.reservaVence ?? null,
    motivoCancelacion: orden.motivoCancelacion ?? null,
    canceladaPor: orden.canceladaPor ?? null,
  }
}

function cambiarEstado(
  orden: OrdenSimulada,
  estado: EstadoOrdenSimulado,
  { fecha = new Date().toISOString(), motivo = null }: { fecha?: string; motivo?: string | null } = {},
): void {
  orden.estado = estado
  orden.historial.push({ estado, fecha })
  if (estado === 'LISTO_PARA_RECOGER') {
    orden.reservaVence = new Date(Date.parse(fecha) + MINUTOS_RESERVA * 60_000).toISOString()
  }
  // Llega a las dos partes, así se actualiza también la pestaña de quien hizo el cambio
  emitirEvento([orden.clienteId, ...duenoDe(orden.tiendaId)], 'estado-orden-cambiado', {
    ordenId: orden.id,
    estado,
    motivo,
  })
}

/**
 * HU-025, CU-30: las recogidas listas que pasaron su reserva quedan VENCIDA. El backend lo hace cada minuto; aquí
 * se revisa al consultar, sin temporizadores (como las solicitudes en actualizarVencimientos).
 */
export function actualizarReservas(ahora = Date.now()): void {
  for (const orden of base.ordenes) {
    if (orden.estado === 'LISTO_PARA_RECOGER' && orden.reservaVence && Date.parse(orden.reservaVence) <= ahora) {
      cambiarEstado(orden, 'VENCIDA', { fecha: orden.reservaVence })
    }
  }
}

/** HU-029: el cliente cancela mientras no salió; la tienda, solo en CONFIRMADO (decisión 1 del contrato). */
function errorAlCancelar(orden: OrdenSimulada, rol: UsuarioSimulado['rol']): Response | null {
  const puede =
    rol === 'CLIENTE'
      ? ['CONFIRMADO', 'PREPARANDO', 'LISTO_PARA_RECOGER'].includes(orden.estado)
      : orden.estado === 'CONFIRMADO'
  if (puede) return null
  if (rol === 'CLIENTE' && (orden.estado === 'EN_CAMINO' || orden.estado === 'ENTREGADO')) {
    return problema(409, 'ORD-005', 'La orden ya fue despachada', {
      detail: 'La orden ya salió de la tienda: escribe a la tienda o reporta un problema.',
    })
  }
  return problema(409, 'ORD-002', 'Cambio de estado no permitido', {
    detail: 'La orden no puede pasar a ese estado desde el actual.',
  })
}

/** Teléfono colombiano en formato internacional sin «+», como lo pide wa.me. */
/** Como EnlaceWhatsapp del backend: solo dígitos y el 57 delante de un celular colombiano (10 dígitos que empiezan por 3). */
function telefonoInternacional(telefono: string): string {
  const digitos = telefono.replace(/\D/g, '')
  return digitos.length === 10 && digitos.startsWith('3') ? `57${digitos}` : digitos
}

/** Con domicilio, el teléfono del cliente es el de la dirección de entrega, quien recibe el pedido (HU-007, CrearOrden). */
function telefonoDelCliente(orden: OrdenSimulada): string {
  const direccion = base.direcciones.find((d) => d.id === orden.direccionId)
  if (orden.modalidad === 'DOMICILIO' && direccion) return direccion.telefono
  return base.usuarios.find((u) => u.id === orden.clienteId)?.telefono ?? ''
}

export const manejadoresOrdenes = [
  // HU-024: elegir una respuesta y crear la orden CONFIRMADO
  http.post('/api/v1/ordenes', async ({ request }) => {
    const cliente = autorizar(request, ['CLIENTE'])
    if (cliente instanceof Response) return cliente
    actualizarVencimientos()

    const datos = await leerJson<CrearOrdenRequest>(request)
    const invalidos = validar([
      ['respuestaId', vacio(datos.respuestaId), 'Elige una respuesta'],
      [
        'modalidad',
        datos.modalidad !== 'RECOGER' && datos.modalidad !== 'DOMICILIO',
        'Elige si vas a recoger o quieres domicilio',
      ],
      ['medioPago', !MEDIOS_PAGO.includes(datos.medioPago as MedioPagoSimulado), 'Elige cómo vas a pagar'],
    ])
    if (invalidos) return invalidos
    const pedido = datos as CrearOrdenRequest
    if (pedido.modalidad === 'DOMICILIO' && vacio(pedido.direccionId)) {
      return problema(400, 'ORD-004', 'Orden incompleta', {
        errores: [{ campo: 'direccionId', mensaje: 'Elige la dirección de entrega' }],
      })
    }

    const respuesta = base.respuestas.find(
      (r) => r.id === pedido.respuestaId && Date.parse(r.fechaRespuesta) <= Date.now(),
    )
    const solicitud = base.solicitudes.find((s) => s.id === respuesta?.solicitudId && s.clienteId === cliente.id)
    if (!respuesta || !solicitud) return problema(404, 'SOL-007', 'Respuesta no encontrada')
    if (solicitud.estado !== 'ABIERTA') {
      return problema(409, 'SOL-004', 'Solicitud cerrada', { detail: 'Esta solicitud ya no está abierta.' })
    }
    if (respuesta.tipo === 'NO') return problema(422, 'SOL-008', 'Respuesta no elegible')

    const tienda = base.tiendas.find((t) => t.id === respuesta.tiendaId)
    if (!tienda) return problema(404, 'SOL-007', 'Respuesta no encontrada')

    // HU-023: producto restringido solo para recoger (ORD-006), antes que el medio de pago, como CrearOrden
    if (solicitud.esRestringida && pedido.modalidad === 'DOMICILIO') {
      return problema(422, 'ORD-006', 'Domicilio no disponible', {
        detail: 'Este producto necesita fórmula médica: solo se puede recoger en la tienda.',
      })
    }

    // Mismo orden que CrearOrden del backend: medio de pago (ORD-003) antes que la cobertura (TIE-010)
    if (!tienda.mediosPago.includes(pedido.medioPago)) {
      return problema(422, 'ORD-003', 'Medio de pago no aceptado', {
        detail: `${tienda.nombre} no recibe ese medio de pago.`,
      })
    }

    let costoEnPesos = 0
    if (pedido.modalidad === 'DOMICILIO') {
      const direccion = base.direcciones.find((d) => d.id === pedido.direccionId && d.clienteId === cliente.id)
      if (!direccion) return problema(404, 'CLI-003', 'Dirección no encontrada')
      const costo = costoDomicilio(tienda, { latitud: direccion.latitud, longitud: direccion.longitud })
      if (costo === null) {
        return problema(422, 'TIE-010', 'Fuera de cobertura', {
          detail: `${tienda.nombre} no lleva domicilio a esa dirección.`,
        })
      }
      costoEnPesos = costo
    }

    const ahora = new Date().toISOString()
    const orden: OrdenSimulada = {
      id: crypto.randomUUID(),
      solicitudId: solicitud.id,
      respuestaId: respuesta.id,
      clienteId: cliente.id,
      tiendaId: tienda.id,
      producto:
        respuesta.tipo === 'PARECIDO' ? (respuesta.descripcionAlternativa ?? solicitud.producto) : solicitud.producto,
      cantidad: respuesta.cantidadDisponible ?? solicitud.cantidad,
      precioUnitarioEnPesos: respuesta.precioEnPesos ?? 0,
      costoDomicilioEnPesos: costoEnPesos,
      modalidad: pedido.modalidad,
      medioPago: pedido.medioPago,
      direccionId: pedido.modalidad === 'DOMICILIO' ? pedido.direccionId : null,
      estado: 'CONFIRMADO',
      fechaCreacion: ahora,
      historial: [{ estado: 'CONFIRMADO', fecha: ahora }],
    }
    base.ordenes.push(orden)
    solicitud.estado = 'RESUELTA'
    solicitud.ordenId = orden.id
    emitirEvento(duenoDe(tienda.id), 'orden-creada', {
      ordenId: orden.id,
      solicitudId: solicitud.id,
      producto: orden.producto,
      totalEnPesos: orden.precioUnitarioEnPesos * orden.cantidad + orden.costoDomicilioEnPesos,
      modalidad: orden.modalidad,
    })
    for (const otra of base.respuestas) {
      if (otra.solicitudId !== solicitud.id || otra.id === respuesta.id || otra.tipo === 'NO') continue
      emitirEvento(duenoDe(otra.tiendaId), 'respuesta-no-elegida', { solicitudId: solicitud.id, respuestaId: otra.id })
    }
    emitirEvento(duenosAlcanzados(solicitud), 'solicitud-cerrada', { solicitudId: solicitud.id, estado: 'RESUELTA' })
    return HttpResponse.json(aOrdenResponse(orden, cliente), {
      status: 201,
      headers: { Location: `/api/v1/ordenes/${orden.id}` },
    })
  }),

  // HU-027: mis órdenes, activas primero y cada grupo de la más reciente a la más antigua
  http.get('/api/v1/ordenes', ({ request }) => {
    const usuario = autorizar(request, ['CLIENTE', 'TIENDA'])
    if (usuario instanceof Response) return usuario
    actualizarReservas()
    const mias = base.ordenes
      .filter((o) => esParteDeLaOrden(o, usuario))
      .sort(
        (a, b) =>
          Number(ordenActiva(b)) - Number(ordenActiva(a)) || Date.parse(b.fechaCreacion) - Date.parse(a.fechaCreacion),
      )
      .slice(0, MAXIMO_ORDENES)
      .map((o) => aOrdenResponse(o, usuario))
    return HttpResponse.json(mias)
  }),

  // HU-024 y HU-027: una orden
  http.get('/api/v1/ordenes/:ordenId', ({ request, params }) => {
    const usuario = autorizar(request, ['CLIENTE', 'TIENDA'])
    if (usuario instanceof Response) return usuario
    actualizarReservas()
    const orden = ordenDe(params.ordenId, usuario)
    if (orden instanceof Response) return orden
    return HttpResponse.json(aOrdenResponse(orden, usuario))
  }),

  // HU-029: cancelar con motivo (3 a 200)
  http.post('/api/v1/ordenes/:ordenId/cancelacion', async ({ request, params }) => {
    const usuario = autorizar(request, ['CLIENTE', 'TIENDA'])
    if (usuario instanceof Response) return usuario
    actualizarReservas()
    const { motivo } = await leerJson<{ motivo: string }>(request)
    const texto = typeof motivo === 'string' ? motivo.trim() : ''
    const largo = texto.length
    const invalidos = validar([
      ['motivo', vacio(motivo), 'Escribe el motivo de la cancelación'],
      ['motivo', !vacio(motivo) && (largo < 3 || largo > 200), 'El motivo debe tener entre 3 y 200 caracteres'],
    ])
    if (invalidos) return invalidos
    const orden = ordenDe(params.ordenId, usuario)
    if (orden instanceof Response) return orden
    const rechazo = errorAlCancelar(orden, usuario.rol)
    if (rechazo) return rechazo
    orden.motivoCancelacion = texto
    orden.canceladaPor = usuario.rol === 'CLIENTE' ? 'CLIENTE' : 'TIENDA'
    cambiarEstado(orden, 'CANCELADO', { motivo: orden.motivoCancelacion })
    return HttpResponse.json(aOrdenResponse(orden, usuario))
  }),

  // HU-027: la tienda avanza solo al paso siguiente válido
  http.patch('/api/v1/ordenes/:ordenId/estado', async ({ request, params }) => {
    const usuario = autorizar(request, ['TIENDA'])
    if (usuario instanceof Response) return usuario
    const { estado } = await leerJson<{ estado: EstadoOrdenSimulado }>(request)
    const invalidos = validar([
      ['estado', !ESTADOS_DE_LA_TIENDA.includes(estado as EstadoOrdenSimulado), 'Elige el estado nuevo'],
    ])
    if (invalidos) return invalidos
    actualizarReservas()
    const orden = ordenDe(params.ordenId, usuario)
    if (orden instanceof Response) return orden
    if (!siguienteEstadoValido(orden, estado as EstadoOrdenSimulado)) {
      return problema(409, 'ORD-002', 'Cambio de estado no permitido', {
        detail: 'La orden no puede pasar a ese estado desde el actual.',
      })
    }
    cambiarEstado(orden, estado as EstadoOrdenSimulado)
    return HttpResponse.json(aOrdenResponse(orden, usuario))
  }),

  // HU-028: «Recibí mi pedido», solo el cliente y solo desde ENTREGADO
  http.post('/api/v1/ordenes/:ordenId/recepcion', ({ request, params }) => {
    const cliente = autorizar(request, ['CLIENTE'])
    if (cliente instanceof Response) return cliente
    actualizarReservas()
    const orden = ordenDe(params.ordenId, cliente)
    if (orden instanceof Response) return orden
    if (orden.estado !== 'ENTREGADO') {
      return problema(409, 'ORD-002', 'Cambio de estado no permitido', {
        detail: 'Puedes confirmar la recepción cuando la tienda marque el pedido como entregado.',
      })
    }
    cambiarEstado(orden, 'RESUELTA')
    return HttpResponse.json(aOrdenResponse(orden, cliente))
  }),

  // HU-032: enlace wa.me de la otra parte, solo con la orden activa
  http.get('/api/v1/ordenes/:ordenId/whatsapp', ({ request, params }) => {
    const usuario = autorizar(request, ['CLIENTE', 'TIENDA'])
    if (usuario instanceof Response) return usuario
    const orden = ordenDe(params.ordenId, usuario)
    if (orden instanceof Response) return orden
    if (!ordenActiva(orden)) return problema(409, 'MEN-001', 'Contacto no disponible')

    const tienda = base.tiendas.find((t) => t.id === orden.tiendaId)
    const otraParte =
      usuario.rol === 'CLIENTE'
        ? { nombre: tienda?.nombre ?? '', telefono: tienda?.telefono ?? '' }
        : {
            nombre: base.usuarios.find((u) => u.id === orden.clienteId)?.nombre ?? '',
            telefono: telefonoDelCliente(orden),
          }
    const telefono = telefonoInternacional(otraParte.telefono)
    const numeroCorto = orden.id.slice(0, 8).toUpperCase()
    // Mismo texto que EnlaceWhatsapp del backend
    const mensaje = `Hola, te escribo por la orden ${numeroCorto} de ${orden.producto} en ¿Quién lo tiene?`
    return HttpResponse.json({
      nombre: otraParte.nombre,
      telefono,
      enlace: `https://wa.me/${telefono}?text=${encodeURIComponent(mensaje)}`,
    })
  }),
]
