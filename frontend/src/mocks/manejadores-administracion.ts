import { http, HttpResponse } from 'msw'
import { base, type ReporteSimulado, type TiendaSimulada } from './datos'
import { autorizar, leerJson, problema, validar } from './utilidades'

/**
 * Administración: revisar, aprobar y rechazar tiendas (HU-036), reportes y suspender o reactivar tiendas (HU-037).
 * Solo para el rol ADMIN.
 */

const RUTA = '/api/v1/admin/tiendas'
const ESTADOS = ['PENDIENTE', 'APROBADA', 'RECHAZADA', 'SUSPENDIDA']

/** TiendaParaRevision del backend. */
function aRevision(tienda: TiendaSimulada) {
  return {
    id: tienda.id,
    nombre: tienda.nombre,
    categoriaId: tienda.categoriaId,
    telefono: tienda.telefono,
    latitud: tienda.latitud,
    longitud: tienda.longitud,
    estado: tienda.estado,
    fechaRegistro: tienda.fechaRegistro,
    motivoRechazo: tienda.motivoRechazo,
    fechaRevision: tienda.fechaRevision,
    documentos: tienda.documentos.map(({ id, tipo, tipoContenido, tamanoEnBytes }) => ({
      id,
      tipo,
      tipoContenido,
      tamanoEnBytes,
    })),
  }
}

function tiendaPendiente(tiendaId: unknown): TiendaSimulada | Response {
  const tienda = base.tiendas.find((t) => t.id === tiendaId)
  if (!tienda) return problema(404, 'TIE-006', 'Tienda no encontrada')
  if (tienda.estado !== 'PENDIENTE') {
    return problema(409, 'TIE-004', 'Cambio de estado no permitido', {
      detail: `La tienda está ${tienda.estado}; solo se revisan las PENDIENTE.`,
    })
  }
  return tienda
}

/** HU-037: ReporteParaAdministrador (ReporteEnPanel), con la tienda y cuántos reportes tiene en total. */
function aReporteParaAdministrador(reporte: ReporteSimulado) {
  const { autorId: _autorId, ...datos } = reporte
  const tienda = base.tiendas.find((t) => t.id === reporte.tiendaId)
  return {
    ...datos,
    // null solo si la tienda ya no existe
    nombreTienda: tienda?.nombre ?? null,
    estadoTienda: tienda?.estado ?? null,
    reportesDeLaTienda: base.reportes.filter((r) => r.tiendaId === reporte.tiendaId).length,
  }
}

/** HU-037 (decisión 7): suspender solo una APROBADA y reactivar solo una SUSPENDIDA; si no, el TIE-004 de siempre. */
function tiendaEn(tiendaId: unknown, estado: TiendaSimulada['estado']): TiendaSimulada | Response {
  const tienda = base.tiendas.find((t) => t.id === tiendaId)
  if (!tienda) return problema(404, 'TIE-006', 'Tienda no encontrada')
  if (tienda.estado !== estado) {
    return problema(409, 'TIE-004', 'Cambio de estado no permitido', { detail: `La tienda está ${tienda.estado}.` })
  }
  return tienda
}

/** La cuenta del dueño queda suspendida o activa con su tienda: suspendida no inicia sesión (IDE-009). */
function suspenderDueno(tienda: TiendaSimulada, suspendido: boolean) {
  const dueno = base.usuarios.find((u) => u.id === tienda.duenoId)
  if (dueno) dueno.estaSuspendido = suspendido
}

export const manejadoresAdministracion = [
  // HU-037: reportes por estado (ABIERTO por defecto), del más antiguo al más reciente
  http.get('/api/v1/admin/reportes', ({ request }) => {
    const admin = autorizar(request, ['ADMIN'])
    if (admin instanceof Response) return admin
    const estado = (new URL(request.url).searchParams.get('estado') ?? 'ABIERTO').toUpperCase()
    const invalidos = validar([
      ['estado', !['ABIERTO', 'RESUELTO'].includes(estado), 'El estado debe ser ABIERTO o RESUELTO'],
    ])
    if (invalidos) return invalidos
    return HttpResponse.json(
      base.reportes
        .filter((r) => r.estado === estado)
        .sort((a, b) => Date.parse(a.fecha) - Date.parse(b.fecha))
        .map(aReporteParaAdministrador),
    )
  }),

  // CP-ADM-037-02: resolver con nota (3 a 500); REP-005 si ya estaba resuelto
  http.post('/api/v1/admin/reportes/:reporteId/resolucion', async ({ request, params }) => {
    const admin = autorizar(request, ['ADMIN'])
    if (admin instanceof Response) return admin
    const { nota } = await leerJson<{ nota: string }>(request)
    const texto = typeof nota === 'string' ? nota.trim() : ''
    const invalidos = validar([
      ['nota', texto.length < 3 || texto.length > 500, 'La nota debe tener entre 3 y 500 caracteres'],
    ])
    if (invalidos) return invalidos
    const reporte = base.reportes.find((r) => r.id === params.reporteId)
    if (!reporte) return problema(404, 'REP-003', 'No encontrado', { detail: 'El reporte no existe.' })
    if (reporte.estado === 'RESUELTO') {
      return problema(409, 'REP-005', 'Reporte ya resuelto', { detail: 'Este reporte ya estaba resuelto.' })
    }
    reporte.estado = 'RESUELTO'
    reporte.notaResolucion = texto
    reporte.fechaResolucion = new Date().toISOString()
    return HttpResponse.json(aReporteParaAdministrador(reporte))
  }),

  // CP-ADM-037-01: suspender una tienda APROBADA con motivo (3 a 300); su dueño ya no inicia sesión
  http.post(`${RUTA}/:tiendaId/suspension`, async ({ request, params }) => {
    const admin = autorizar(request, ['ADMIN'])
    if (admin instanceof Response) return admin
    const { motivo } = await leerJson<{ motivo: string }>(request)
    const largo = typeof motivo === 'string' ? motivo.trim().length : 0
    const invalidos = validar([['motivo', largo < 3 || largo > 300, 'El motivo debe tener entre 3 y 300 caracteres']])
    if (invalidos) return invalidos
    const tienda = tiendaEn(params.tiendaId, 'APROBADA')
    if (tienda instanceof Response) return tienda
    tienda.estado = 'SUSPENDIDA'
    tienda.estaAbierta = false
    suspenderDueno(tienda, true)
    return HttpResponse.json(aRevision(tienda))
  }),

  http.post(`${RUTA}/:tiendaId/reactivacion`, ({ request, params }) => {
    const admin = autorizar(request, ['ADMIN'])
    if (admin instanceof Response) return admin
    const tienda = tiendaEn(params.tiendaId, 'SUSPENDIDA')
    if (tienda instanceof Response) return tienda
    tienda.estado = 'APROBADA'
    suspenderDueno(tienda, false)
    return HttpResponse.json(aRevision(tienda))
  }),

  http.get(RUTA, ({ request }) => {
    const admin = autorizar(request, ['ADMIN'])
    if (admin instanceof Response) return admin
    const estado = new URL(request.url).searchParams.get('estado') ?? 'PENDIENTE'
    const invalidos = validar([
      ['estado', !ESTADOS.includes(estado), 'El estado debe ser PENDIENTE, APROBADA, RECHAZADA o SUSPENDIDA'],
    ])
    if (invalidos) return invalidos
    return HttpResponse.json(base.tiendas.filter((t) => t.estado === estado).map(aRevision))
  }),

  http.get(`${RUTA}/:tiendaId/documentos/:documentoId`, ({ request, params }) => {
    const admin = autorizar(request, ['ADMIN'])
    if (admin instanceof Response) return admin
    const documento = base.tiendas
      .find((t) => t.id === params.tiendaId)
      ?.documentos.find((d) => d.id === params.documentoId)
    if (!documento) return problema(404, 'TIE-006', 'Tienda no encontrada')
    return new HttpResponse(documento.contenido, {
      headers: {
        'Content-Type': documento.tipoContenido,
        'Content-Disposition': `attachment; filename="${documento.nombreArchivo}"`,
        'Cache-Control': 'no-store',
      },
    })
  }),

  http.post(`${RUTA}/:tiendaId/aprobacion`, ({ request, params }) => {
    const admin = autorizar(request, ['ADMIN'])
    if (admin instanceof Response) return admin
    const tienda = tiendaPendiente(params.tiendaId)
    if (tienda instanceof Response) return tienda
    tienda.estado = 'APROBADA'
    tienda.fechaRevision = new Date().toISOString()
    return HttpResponse.json(aRevision(tienda))
  }),

  http.post(`${RUTA}/:tiendaId/rechazo`, async ({ request, params }) => {
    const admin = autorizar(request, ['ADMIN'])
    if (admin instanceof Response) return admin
    const { motivo } = await leerJson<{ motivo: string }>(request)
    const largo = (motivo ?? '').trim().length
    const invalidos = validar([['motivo', largo < 5 || largo > 300, 'El motivo debe tener entre 5 y 300 caracteres']])
    if (invalidos) return invalidos
    const tienda = tiendaPendiente(params.tiendaId)
    if (tienda instanceof Response) return tienda
    tienda.estado = 'RECHAZADA'
    tienda.motivoRechazo = (motivo as string).trim()
    tienda.fechaRevision = new Date().toISOString()
    return HttpResponse.json(aRevision(tienda))
  }),
]
