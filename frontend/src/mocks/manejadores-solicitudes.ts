import { http, HttpResponse } from 'msw'
import { base, type SolicitudSimulada } from './datos'
import { emitirEvento } from './eventos'
import {
  actualizarVencimientos,
  aSolicitudResponse,
  distanciaAproximada,
  distanciaEnMetros,
  esProductoRestringido,
  llegoALaTienda,
  respuestasAutomaticas,
  respuestasVisibles,
} from './reglas-sprint-2'
import { autorizar, leerJson, problema, ubicacionInvalida, vacio, validar } from './utilidades'

/**
 * Solicitudes: publicar «¿quién tiene X?» (HU-016), máximo 10 por hora y vence a los 30 minutos; historial y
 * repetir (HU-008, SC-018).
 */

const RUTA = '/api/v1/solicitudes'
const LIMITE_POR_HORA = 10
const UNA_HORA_MS = 60 * 60 * 1000
const TREINTA_MINUTOS_MS = 30 * 60 * 1000
const RADIO_INICIAL_EN_METROS = 3000

interface PublicarSolicitudRequest {
  producto: string
  cantidad: number
  categoriaId: string
  modalidad: 'RECOGER' | 'DOMICILIO'
  esUrgente?: boolean | null
  ubicacion: { latitud: number; longitud: number }
}

/** HU-018 y HU-030: cada tienda abierta a la que llegó recibe solicitud-nueva con su distancia aproximada. */
export function avisarSolicitudNueva(solicitud: SolicitudSimulada): void {
  for (const tienda of base.tiendas) {
    if (!tienda.estaAbierta || !llegoALaTienda(solicitud, tienda)) continue
    emitirEvento([tienda.duenoId], 'solicitud-nueva', {
      solicitudId: solicitud.id,
      producto: solicitud.producto,
      cantidad: solicitud.cantidad,
      modalidad: solicitud.modalidad,
      esUrgente: solicitud.esUrgente,
      distanciaEnMetros: distanciaAproximada(
        distanciaEnMetros(solicitud.ubicacion, { latitud: tienda.latitud, longitud: tienda.longitud }),
      ),
      fechaVencimiento: solicitud.fechaVencimiento,
    })
  }
}

/** SOL-001 si el cliente ya publicó 10 en la última hora (también cuenta repetir, HU-008). */
function limiteSuperado(clienteId: string, ahora: number): Response | null {
  const deLaUltimaHora = base.solicitudes
    .filter((s) => s.clienteId === clienteId && ahora - Date.parse(s.fechaPublicacion) < UNA_HORA_MS)
    .map((s) => Date.parse(s.fechaPublicacion))
    .sort((a, b) => a - b)
  const masAntigua = deLaUltimaHora[0]
  if (deLaUltimaHora.length < LIMITE_POR_HORA || masAntigua === undefined) return null
  const segundos = Math.max(1, Math.ceil((masAntigua + UNA_HORA_MS - ahora) / 1000))
  const minutos = Math.ceil(segundos / 60)
  return problema(429, 'SOL-001', 'Límite de solicitudes superado', {
    detail: `Ya publicaste 10 solicitudes en la última hora. Puedes publicar otra en ${minutos === 1 ? '1 minuto' : `${minutos} minutos`}.`,
    cabeceras: { 'Retry-After': String(segundos) },
  })
}

/** Publica la solicitud: la guarda, la difunde a las tiendas y programa las respuestas simuladas. */
function publicar(clienteId: string, datos: PublicarSolicitudRequest, ahora: number): SolicitudSimulada {
  const tiendasNotificadas = base.tiendas.filter(
    (t) => t.estado === 'APROBADA' && t.estaAbierta && t.categoriaId === datos.categoriaId,
  ).length
  const solicitud: SolicitudSimulada = {
    id: crypto.randomUUID(),
    clienteId,
    categoriaId: datos.categoriaId,
    producto: datos.producto.trim(),
    cantidad: datos.cantidad,
    modalidad: datos.modalidad,
    esUrgente: datos.esUrgente ?? false,
    ubicacion: datos.ubicacion,
    radioEnMetros: RADIO_INICIAL_EN_METROS,
    estado: 'ABIERTA',
    fechaPublicacion: new Date(ahora).toISOString(),
    fechaVencimiento: new Date(ahora + TREINTA_MINUTOS_MS).toISOString(),
    tiendasNotificadas,
    ordenId: null,
    // HU-023: la marca queda en la solicitud; cambiar la lista después no la cambia (decisión 5)
    esRestringida: esProductoRestringido(datos.producto),
  }
  base.solicitudes.push(solicitud)
  const automaticas = respuestasAutomaticas(solicitud, ahora)
  base.respuestas.push(...automaticas)
  avisarSolicitudNueva(solicitud)
  for (const respuesta of automaticas) {
    const tienda = base.tiendas.find((t) => t.id === respuesta.tiendaId)
    setTimeout(
      () =>
        emitirEvento([solicitud.clienteId], 'respuesta-recibida', {
          solicitudId: solicitud.id,
          respuestaId: respuesta.id,
          tipo: respuesta.tipo,
          nombreTienda: tienda?.nombre ?? '',
        }),
      Date.parse(respuesta.fechaRespuesta) - ahora,
    )
  }
  return solicitud
}

const respuestaPublicada = (solicitud: SolicitudSimulada) =>
  HttpResponse.json(aSolicitudResponse(solicitud), { status: 201, headers: { Location: `${RUTA}/${solicitud.id}` } })

/**
 * HU-008 y SC-018: el historial del cliente, de la más reciente a la más antigua, paginado (tamaño 1 a 50). Va antes
 * de las rutas con :solicitudId para que «mias» no se tome como un id.
 */
export const manejadoresHistorial = [
  http.get(`${RUTA}/mias`, ({ request }) => {
    const cliente = autorizar(request, ['CLIENTE'])
    if (cliente instanceof Response) return cliente
    actualizarVencimientos()
    const parametros = new URL(request.url).searchParams
    const pagina = Number(parametros.get('pagina') ?? 0)
    const tamano = Number(parametros.get('tamano') ?? 20)
    const invalidos = validar([
      ['pagina', !Number.isInteger(pagina) || pagina < 0, 'La página debe ser 0 o más'],
      ['tamano', !Number.isInteger(tamano) || tamano < 1 || tamano > 50, 'El tamaño debe estar entre 1 y 50'],
    ])
    if (invalidos) return invalidos
    const ahora = Date.now()
    const mias = base.solicitudes
      .filter((s) => s.clienteId === cliente.id)
      .sort((a, b) => Date.parse(b.fechaPublicacion) - Date.parse(a.fechaPublicacion))
    const pedazo = mias.slice(pagina * tamano, (pagina + 1) * tamano)
    return HttpResponse.json({
      pagina,
      hayMas: (pagina + 1) * tamano < mias.length,
      solicitudes: pedazo.map((s) => ({
        id: s.id,
        categoriaId: s.categoriaId,
        producto: s.producto,
        cantidad: s.cantidad,
        modalidad: s.modalidad,
        esUrgente: s.esUrgente,
        estado: s.estado,
        // Abierta y sin vencer, aunque la tarea de vencimiento todavía no la haya pasado a VENCIDA (decisión 10)
        estaAbierta: s.estado === 'ABIERTA' && Date.parse(s.fechaVencimiento) > ahora,
        fechaPublicacion: s.fechaPublicacion,
        fechaVencimiento: s.fechaVencimiento,
        respuestasRecibidas: respuestasVisibles(s.id).length,
        ordenId: s.ordenId,
        esRestringida: s.esRestringida ?? false,
      })),
    })
  }),

  // HU-008, CP-CLI-008-01: otra igual (producto, cantidad, categoría, modalidad y urgencia) con la ubicación de ahora
  http.post(`${RUTA}/:solicitudId/repeticion`, async ({ request, params }) => {
    const cliente = autorizar(request, ['CLIENTE'])
    if (cliente instanceof Response) return cliente
    const { ubicacion } = await leerJson<{ ubicacion: { latitud: number; longitud: number } }>(request)
    const invalidos = validar([['ubicacion', ubicacionInvalida(ubicacion), 'Comparte tu ubicación']])
    if (invalidos) return invalidos
    const original = base.solicitudes.find((s) => s.id === params.solicitudId && s.clienteId === cliente.id)
    if (!original) return problema(404, 'SOL-003', 'Solicitud no encontrada')
    const ahora = Date.now()
    const limite = limiteSuperado(cliente.id, ahora)
    if (limite) return limite
    const repetida = publicar(
      cliente.id,
      {
        producto: original.producto,
        cantidad: original.cantidad,
        categoriaId: original.categoriaId,
        modalidad: original.modalidad,
        esUrgente: original.esUrgente,
        ubicacion: ubicacion as { latitud: number; longitud: number },
      },
      ahora,
    )
    return respuestaPublicada(repetida)
  }),
]

export const manejadoresSolicitudes = [
  http.post(RUTA, async ({ request }) => {
    const cliente = autorizar(request, ['CLIENTE'])
    if (cliente instanceof Response) return cliente

    const datos = await leerJson<PublicarSolicitudRequest>(request)
    const cantidad = datos.cantidad
    const invalidos = validar([
      [
        'producto',
        vacio(datos.producto) || (datos.producto ?? '').length > 150,
        'Escribe el producto que buscas (máximo 150 caracteres)',
      ],
      [
        'cantidad',
        typeof cantidad !== 'number' || !Number.isInteger(cantidad) || cantidad < 1 || cantidad > 999,
        'La cantidad debe estar entre 1 y 999',
      ],
      ['categoriaId', vacio(datos.categoriaId), 'Elige la categoría'],
      [
        'modalidad',
        datos.modalidad !== 'RECOGER' && datos.modalidad !== 'DOMICILIO',
        'Elige si vas a recoger o quieres domicilio',
      ],
      ['ubicacion', ubicacionInvalida(datos.ubicacion), 'Comparte tu ubicación'],
    ])
    if (invalidos) return invalidos

    const ahora = Date.now()
    const limite = limiteSuperado(cliente.id, ahora)
    if (limite) return limite
    return respuestaPublicada(publicar(cliente.id, datos as PublicarSolicitudRequest, ahora))
  }),
]
