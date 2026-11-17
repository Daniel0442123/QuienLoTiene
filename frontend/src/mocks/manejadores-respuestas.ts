import { http, HttpResponse } from 'msw'
import { base, type RespuestaSimulada, type SolicitudSimulada } from './datos'
import { emitirEvento } from './eventos'
import { miTienda } from './manejadores-tiendas'
import {
  actualizarVencimientos,
  aSolicitudResponse,
  costoDomicilio,
  distanciaAproximada,
  distanciaEnMetros,
  estadoDeRespuesta,
  llegoALaTienda,
  normalizarProducto,
  respuestasVisibles,
  ubicacionDeTienda,
} from './reglas-sprint-2'
import { autorizar, leerJson, problema, validar } from './utilidades'

/**
 * Solicitudes para la tienda y respuestas (HU-018, HU-019, HU-020 y HU-021), según docs/api/contrato-sprint-2.yaml.
 */

const VEINTICUATRO_HORAS_MS = 24 * 60 * 60 * 1000
const ORDENES_VALIDOS = ['precio', 'distancia', 'total'] as const

interface ResponderSolicitudRequest {
  tipo: 'SI' | 'PARECIDO' | 'NO'
  precioEnPesos: number | null
  cantidadDisponible: number | null
  descripcionAlternativa: string | null
  tiempoEstimadoEnMinutos: number | null
}

const entero = (valor: unknown): valor is number => typeof valor === 'number' && Number.isInteger(valor)
const nulo = (valor: unknown) => valor === null || valor === undefined

/** Solicitud del cliente dueño, o 404 SOL-003 si no existe o es de otro (riesgo R-03: 404, no 403). */
function solicitudDelCliente(id: unknown, clienteId: string): SolicitudSimulada | Response {
  const solicitud = base.solicitudes.find((s) => s.id === id && s.clienteId === clienteId)
  return solicitud ?? problema(404, 'SOL-003', 'Solicitud no encontrada')
}

function aRespuestaDeTienda(respuesta: RespuestaSimulada, solicitud: SolicitudSimulada) {
  const estado = estadoDeRespuesta(respuesta, solicitud)
  return {
    id: respuesta.id,
    solicitudId: solicitud.id,
    producto: solicitud.producto,
    cantidadSolicitada: solicitud.cantidad,
    tipo: respuesta.tipo,
    precioEnPesos: respuesta.precioEnPesos,
    cantidadDisponible: respuesta.cantidadDisponible,
    descripcionAlternativa: respuesta.descripcionAlternativa,
    tiempoEstimadoEnMinutos: respuesta.tiempoEstimadoEnMinutos,
    ofreceDomicilio: respuesta.ofreceDomicilio,
    costoDomicilioEnPesos: respuesta.costoDomicilioEnPesos,
    estado,
    fechaRespuesta: respuesta.fechaRespuesta,
    ordenId: estado === 'ELEGIDA' ? solicitud.ordenId : null,
  }
}

function aTarjetaParaCliente(respuesta: RespuestaSimulada, solicitud: SolicitudSimulada) {
  const tienda = base.tiendas.find((t) => t.id === respuesta.tiendaId)
  const precio = respuesta.precioEnPesos ?? 0
  const cantidad = respuesta.cantidadDisponible ?? 0
  const subtotal = precio * cantidad
  const domicilio =
    solicitud.modalidad === 'DOMICILIO' && respuesta.ofreceDomicilio ? (respuesta.costoDomicilioEnPesos ?? 0) : 0
  const orden = base.ordenes.find((o) => o.id === solicitud.ordenId)
  return {
    id: respuesta.id,
    tiendaId: respuesta.tiendaId,
    nombreTienda: tienda?.nombre ?? 'Tienda',
    tipo: respuesta.tipo as 'SI' | 'PARECIDO',
    esAlternativa: respuesta.tipo === 'PARECIDO',
    descripcionAlternativa: respuesta.descripcionAlternativa,
    precioEnPesos: precio,
    cantidadDisponible: cantidad,
    subtotalEnPesos: subtotal,
    ofreceDomicilio: respuesta.ofreceDomicilio,
    costoDomicilioEnPesos: respuesta.costoDomicilioEnPesos,
    totalEnPesos: subtotal + domicilio,
    distanciaEnMetros: tienda ? distanciaEnMetros(solicitud.ubicacion, ubicacionDeTienda(tienda)) : 0,
    tiempoEstimadoEnMinutos: respuesta.tiempoEstimadoEnMinutos,
    fechaRespuesta: respuesta.fechaRespuesta,
    fueElegida: orden?.respuestaId === respuesta.id,
  }
}

export const manejadoresRespuestas = [
  // HU-018: solicitudes abiertas que llegaron a mi tienda y no he respondido; urgentes primero
  http.get('/api/v1/solicitudes/disponibles', ({ request }) => {
    const tienda = miTienda(request)
    if (tienda instanceof Response) return tienda
    actualizarVencimientos()

    const disponibles = base.solicitudes
      .filter((s) => s.estado === 'ABIERTA' && llegoALaTienda(s, tienda))
      .filter((s) => !base.respuestas.some((r) => r.solicitudId === s.id && r.tiendaId === tienda.id))
      .sort(
        (a, b) =>
          Number(b.esUrgente) - Number(a.esUrgente) || Date.parse(b.fechaPublicacion) - Date.parse(a.fechaPublicacion),
      )
      .map((s) => {
        const anterior = base.respuestas
          .filter(
            (r) =>
              r.tiendaId === tienda.id &&
              r.precioEnPesos !== null &&
              normalizarProducto(base.solicitudes.find((otra) => otra.id === r.solicitudId)?.producto ?? '') ===
                normalizarProducto(s.producto),
          )
          .sort((a, b) => Date.parse(b.fechaRespuesta) - Date.parse(a.fechaRespuesta))[0]
        return {
          id: s.id,
          categoriaId: s.categoriaId,
          producto: s.producto,
          cantidad: s.cantidad,
          modalidad: s.modalidad,
          esUrgente: s.esUrgente,
          distanciaEnMetros: distanciaAproximada(distanciaEnMetros(s.ubicacion, ubicacionDeTienda(tienda))),
          fechaPublicacion: s.fechaPublicacion,
          fechaVencimiento: s.fechaVencimiento,
          precioSugeridoEnPesos: anterior?.precioEnPesos ?? null,
        }
      })
    return HttpResponse.json(disponibles)
  }),

  // HU-019: responder SI, PARECIDO o NO, una sola vez
  http.post('/api/v1/solicitudes/:solicitudId/respuestas', async ({ request, params }) => {
    const tienda = miTienda(request)
    if (tienda instanceof Response) return tienda
    actualizarVencimientos()

    const solicitud = base.solicitudes.find((s) => s.id === params.solicitudId)
    if (!solicitud || !llegoALaTienda(solicitud, tienda)) return problema(404, 'SOL-003', 'Solicitud no encontrada')

    const datos = await leerJson<ResponderSolicitudRequest>(request)
    const invalidos = validar([
      ['tipo', !['SI', 'PARECIDO', 'NO'].includes(datos.tipo ?? ''), 'Elige qué respondes'],
      [
        'precioEnPesos',
        !nulo(datos.precioEnPesos) &&
          (!entero(datos.precioEnPesos) || datos.precioEnPesos < 1 || datos.precioEnPesos > 100_000_000),
        'Escribe un precio entre $ 1 y $ 100.000.000',
      ],
      [
        'tiempoEstimadoEnMinutos',
        !nulo(datos.tiempoEstimadoEnMinutos) &&
          (!entero(datos.tiempoEstimadoEnMinutos) ||
            datos.tiempoEstimadoEnMinutos < 1 ||
            datos.tiempoEstimadoEnMinutos > 240),
        'El tiempo debe estar entre 1 y 240 minutos',
      ],
    ])
    if (invalidos) return invalidos

    if (base.respuestas.some((r) => r.solicitudId === solicitud.id && r.tiendaId === tienda.id)) {
      return problema(409, 'SOL-005', 'Respuesta duplicada', { detail: 'Ya respondiste esta solicitud.' })
    }
    if (solicitud.estado !== 'ABIERTA') {
      return problema(409, 'SOL-004', 'Solicitud cerrada', { detail: 'Esta solicitud ya no recibe respuestas.' })
    }

    const tipo = datos.tipo as ResponderSolicitudRequest['tipo']
    if (tipo !== 'NO') {
      const cantidad = datos.cantidadDisponible
      const errores = [
        nulo(datos.precioEnPesos) && { campo: 'precioEnPesos', mensaje: 'Escribe el precio por unidad' },
        (!entero(cantidad) || cantidad < 1 || cantidad > solicitud.cantidad) && {
          campo: 'cantidadDisponible',
          mensaje: `Escribe cuántas unidades tienes, de 1 a ${solicitud.cantidad}`,
        },
        tipo === 'PARECIDO' &&
          (typeof datos.descripcionAlternativa !== 'string' ||
            datos.descripcionAlternativa.trim() === '' ||
            datos.descripcionAlternativa.length > 300) && {
            campo: 'descripcionAlternativa',
            mensaje: 'Describe qué producto ofreces (máximo 300 caracteres)',
          },
      ].filter((error) => error !== false)
      if (errores.length > 0) return problema(400, 'SOL-006', 'Respuesta incompleta', { errores })
    }

    const costo = tipo === 'NO' ? null : costoDomicilio(tienda, solicitud.ubicacion)
    const respuesta: RespuestaSimulada = {
      id: crypto.randomUUID(),
      solicitudId: solicitud.id,
      tiendaId: tienda.id,
      tipo,
      precioEnPesos: tipo === 'NO' ? null : (datos.precioEnPesos ?? null),
      cantidadDisponible: tipo === 'NO' ? null : (datos.cantidadDisponible ?? null),
      descripcionAlternativa: tipo === 'PARECIDO' ? (datos.descripcionAlternativa ?? '').trim() : null,
      tiempoEstimadoEnMinutos: tipo === 'NO' ? null : (datos.tiempoEstimadoEnMinutos ?? null),
      ofreceDomicilio: costo !== null,
      costoDomicilioEnPesos: costo,
      fechaRespuesta: new Date().toISOString(),
    }
    base.respuestas.push(respuesta)
    if (tipo !== 'NO') {
      emitirEvento([solicitud.clienteId], 'respuesta-recibida', {
        solicitudId: solicitud.id,
        respuestaId: respuesta.id,
        tipo,
        nombreTienda: tienda.nombre,
      })
    }
    return HttpResponse.json(aRespuestaDeTienda(respuesta, solicitud), {
      status: 201,
      headers: { Location: `/api/v1/tiendas/me/respuestas/${respuesta.id}` },
    })
  }),

  // HU-019 y HU-021: «Mis respuestas» SI y PARECIDO de las últimas 24 h, la más reciente primero
  http.get('/api/v1/tiendas/me/respuestas', ({ request }) => {
    const tienda = miTienda(request)
    if (tienda instanceof Response) return tienda
    actualizarVencimientos()

    const desde = Date.now() - VEINTICUATRO_HORAS_MS
    const mias = base.respuestas
      .filter((r) => r.tiendaId === tienda.id && r.tipo !== 'NO' && Date.parse(r.fechaRespuesta) >= desde)
      .sort((a, b) => Date.parse(b.fechaRespuesta) - Date.parse(a.fechaRespuesta))
      .flatMap((r) => {
        const solicitud = base.solicitudes.find((s) => s.id === r.solicitudId)
        return solicitud ? [aRespuestaDeTienda(r, solicitud)] : []
      })
    return HttpResponse.json(mias)
  }),

  // HU-020 y HU-021: mi solicitud con su estado
  http.get('/api/v1/solicitudes/:solicitudId', ({ request, params }) => {
    const cliente = autorizar(request, ['CLIENTE'])
    if (cliente instanceof Response) return cliente
    actualizarVencimientos()

    const solicitud = solicitudDelCliente(params.solicitudId, cliente.id)
    if (solicitud instanceof Response) return solicitud
    return HttpResponse.json(aSolicitudResponse(solicitud))
  }),

  // HU-020: tarjetas SI y PARECIDO ordenadas; las NO solo cuentan
  http.get('/api/v1/solicitudes/:solicitudId/respuestas', ({ request, params }) => {
    const cliente = autorizar(request, ['CLIENTE'])
    if (cliente instanceof Response) return cliente
    actualizarVencimientos()

    const orden = new URL(request.url).searchParams.get('orden') ?? 'precio'
    if (!ORDENES_VALIDOS.includes(orden as (typeof ORDENES_VALIDOS)[number])) {
      return problema(400, 'GEN-002', 'Petición mal formada', { detail: 'orden debe ser precio, distancia o total.' })
    }
    const solicitud = solicitudDelCliente(params.solicitudId, cliente.id)
    if (solicitud instanceof Response) return solicitud

    const recibidas = respuestasVisibles(solicitud.id)
    const criterio = {
      precio: 'precioEnPesos',
      distancia: 'distanciaEnMetros',
      total: 'totalEnPesos',
    } as const satisfies Record<(typeof ORDENES_VALIDOS)[number], string>
    const campo = criterio[orden as keyof typeof criterio]
    const tarjetas = recibidas
      .filter((r) => r.tipo !== 'NO')
      .map((r) => aTarjetaParaCliente(r, solicitud))
      .sort((a, b) => a[campo] - b[campo])

    return HttpResponse.json({
      solicitudId: solicitud.id,
      estadoSolicitud: solicitud.estado,
      cantidadSolicitada: solicitud.cantidad,
      respuestasRecibidas: recibidas.length,
      respuestas: tarjetas,
    })
  }),
]
