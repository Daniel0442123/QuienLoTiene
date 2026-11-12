import { http, HttpResponse } from 'msw'
import { base, type TiendaSimulada } from './datos'
import { autorizar, leerJson, problema, validar } from './utilidades'

/** Administración: revisar, aprobar y rechazar tiendas (HU-036). Solo para el rol ADMIN. */

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

export const manejadoresAdministracion = [
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
