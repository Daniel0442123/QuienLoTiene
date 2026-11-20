import { http, HttpResponse } from 'msw'
import { base, type MotivoReporteSimulado, type ReporteSimulado, type ResenaSimulada } from './datos'
import { actualizarReservas, ordenDe } from './manejadores-ordenes'
import { autorizar, leerJson, problema, validar } from './utilidades'

const MOTIVOS: MotivoReporteSimulado[] = ['DEMORA_ENTREGA', 'PRODUCTO_DIFERENTE', 'COBRO_DIFERENTE', 'OTRO']
const LARGO_MAXIMO_DESCRIPCION = 1000

/**
 * Reputación (HU-034 y HU-014), como CalificarTienda y ConsultarReputacion del backend: una reseña por orden
 * RESUELTA del cliente, y el promedio con un decimal (0 sin reseñas) con las 10 más recientes, sin datos del cliente.
 */

const LARGO_MAXIMO_COMENTARIO = 500
const RECIENTES = 10

function aResenaResponse(resena: ResenaSimulada) {
  return {
    id: resena.id,
    ordenId: resena.ordenId,
    tiendaId: resena.tiendaId,
    estrellas: resena.estrellas,
    comentario: resena.comentario,
    fecha: resena.fecha,
  }
}

export const manejadoresReputacion = [
  // HU-035: reportar un problema de una orden en la que participa (cliente o tienda); queda ABIERTO
  http.post('/api/v1/ordenes/:ordenId/reportes', async ({ request, params }) => {
    const usuario = autorizar(request, ['CLIENTE', 'TIENDA'])
    if (usuario instanceof Response) return usuario
    const { motivo, descripcion } = await leerJson<{ motivo: string; descripcion?: string | null }>(request)
    const invalidos = validar([
      [
        'motivo',
        !MOTIVOS.includes(motivo as MotivoReporteSimulado),
        'El motivo debe ser DEMORA_ENTREGA, PRODUCTO_DIFERENTE, COBRO_DIFERENTE u OTRO',
      ],
      [
        'descripcion',
        typeof descripcion === 'string' && descripcion.length > LARGO_MAXIMO_DESCRIPCION,
        'La descripción puede tener hasta 1000 caracteres',
      ],
    ])
    if (invalidos) return invalidos
    // CP-REP-035-02: una orden ajena responde 404 ORD-001, sin decir que existe
    const orden = ordenDe(params.ordenId, usuario)
    if (orden instanceof Response) return orden
    const limpia = typeof descripcion === 'string' && descripcion.trim() !== '' ? descripcion.trim() : null
    if (motivo === 'OTRO' && limpia === null) {
      return problema(400, 'REP-004', 'Datos inválidos', { detail: 'Revisa los datos: cuéntanos qué pasó.' })
    }
    const reporte: ReporteSimulado = {
      id: crypto.randomUUID(),
      ordenId: orden.id,
      tiendaId: orden.tiendaId,
      autorId: usuario.id,
      reportadoPor: usuario.rol === 'CLIENTE' ? 'CLIENTE' : 'TIENDA',
      motivo: motivo as MotivoReporteSimulado,
      descripcion: limpia,
      estado: 'ABIERTO',
      fecha: new Date().toISOString(),
      notaResolucion: null,
      fechaResolucion: null,
    }
    base.reportes.push(reporte)
    return HttpResponse.json(
      {
        id: reporte.id,
        ordenId: reporte.ordenId,
        motivo: reporte.motivo,
        descripcion: reporte.descripcion,
        estado: reporte.estado,
        fecha: reporte.fecha,
      },
      { status: 201 },
    )
  }),

  // HU-034: calificar la tienda de una orden RESUELTA, una sola vez
  http.post('/api/v1/ordenes/:ordenId/resena', async ({ request, params }) => {
    const cliente = autorizar(request, ['CLIENTE'])
    if (cliente instanceof Response) return cliente
    const { estrellas, comentario } = await leerJson<{ estrellas: number; comentario?: string | null }>(request)
    const invalidos = validar([
      [
        'estrellas',
        !Number.isInteger(estrellas) || (estrellas as number) < 1 || (estrellas as number) > 5,
        'Elige de 1 a 5 estrellas',
      ],
      [
        'comentario',
        typeof comentario === 'string' && comentario.length > LARGO_MAXIMO_COMENTARIO,
        'El comentario puede tener hasta 500 caracteres',
      ],
    ])
    if (invalidos) return invalidos

    actualizarReservas()
    const orden = ordenDe(params.ordenId, cliente)
    if (orden instanceof Response) return orden
    // Mismo orden que el backend: primero si se puede calificar (REP-001), después si ya calificó (REP-002)
    if (orden.clienteId !== cliente.id || orden.estado !== 'RESUELTA') {
      return problema(422, 'REP-001', 'Calificación no permitida', {
        detail: 'Solo puedes calificar un pedido tuyo que ya recibiste.',
      })
    }
    if (base.resenas.some((r) => r.ordenId === orden.id)) {
      return problema(409, 'REP-002', 'Reseña duplicada', { detail: 'Ya calificaste este pedido.' })
    }
    const texto = typeof comentario === 'string' ? comentario.trim() : ''
    const resena: ResenaSimulada = {
      id: crypto.randomUUID(),
      ordenId: orden.id,
      tiendaId: orden.tiendaId,
      clienteId: cliente.id,
      estrellas: estrellas as number,
      comentario: texto === '' ? null : texto,
      fecha: new Date().toISOString(),
    }
    base.resenas.push(resena)
    return HttpResponse.json(aResenaResponse(resena), {
      status: 201,
      headers: { Location: `/api/v1/ordenes/${orden.id}/resena` },
    })
  }),

  // HU-034: la reseña de una orden, para su cliente o su tienda; REP-003 si todavía no tiene
  http.get('/api/v1/ordenes/:ordenId/resena', ({ request, params }) => {
    const usuario = autorizar(request, ['CLIENTE', 'TIENDA'])
    if (usuario instanceof Response) return usuario
    const orden = ordenDe(params.ordenId, usuario)
    if (orden instanceof Response) return orden
    const resena = base.resenas.find((r) => r.ordenId === orden.id)
    if (!resena) return problema(404, 'REP-003', 'Reseña no encontrada', { detail: 'La reseña de esta orden' })
    return HttpResponse.json(aResenaResponse(resena))
  }),

  // HU-014: promedio, total y las 10 reseñas más recientes de una tienda, sin el nombre del cliente
  http.get('/api/v1/tiendas/:tiendaId/resenas', ({ request, params }) => {
    const usuario = autorizar(request, ['CLIENTE', 'TIENDA', 'ADMIN'])
    if (usuario instanceof Response) return usuario
    const deLaTienda = base.resenas
      .filter((r) => r.tiendaId === params.tiendaId)
      .sort((a, b) => Date.parse(b.fecha) - Date.parse(a.fecha))
    const suma = deLaTienda.reduce((total, r) => total + r.estrellas, 0)
    return HttpResponse.json({
      tiendaId: params.tiendaId,
      promedio: deLaTienda.length === 0 ? 0 : Math.round((suma / deLaTienda.length) * 10) / 10,
      totalResenas: deLaTienda.length,
      recientes: deLaTienda
        .slice(0, RECIENTES)
        .map(({ estrellas, comentario, fecha }) => ({ estrellas, comentario, fecha })),
    })
  }),
]
