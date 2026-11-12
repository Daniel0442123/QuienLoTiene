import { http, HttpResponse } from 'msw'
import { base, type SolicitudSimulada } from './datos'
import { autorizar, leerJson, problema, ubicacionInvalida, vacio, validar } from './utilidades'

/** Solicitudes: publicar «¿quién tiene X?» (HU-016). Máximo 10 por hora; vence a los 30 minutos. */

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

function aRespuesta(solicitud: SolicitudSimulada) {
  const { clienteId: _clienteId, ...datos } = solicitud
  return datos
}

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
    const deLaUltimaHora = base.solicitudes
      .filter((s) => s.clienteId === cliente.id && ahora - Date.parse(s.fechaPublicacion) < UNA_HORA_MS)
      .map((s) => Date.parse(s.fechaPublicacion))
      .sort((a, b) => a - b)
    const masAntigua = deLaUltimaHora[0]
    if (deLaUltimaHora.length >= LIMITE_POR_HORA && masAntigua !== undefined) {
      const segundos = Math.max(1, Math.ceil((masAntigua + UNA_HORA_MS - ahora) / 1000))
      const minutos = Math.ceil(segundos / 60)
      return problema(429, 'SOL-001', 'Límite de solicitudes superado', {
        detail: `Ya publicaste 10 solicitudes en la última hora. Puedes publicar otra en ${minutos === 1 ? '1 minuto' : `${minutos} minutos`}.`,
        cabeceras: { 'Retry-After': String(segundos) },
      })
    }

    const completos = datos as PublicarSolicitudRequest
    const tiendasNotificadas = base.tiendas.filter(
      (t) => t.estado === 'APROBADA' && t.estaAbierta && t.categoriaId === completos.categoriaId,
    ).length
    const solicitud: SolicitudSimulada = {
      id: crypto.randomUUID(),
      clienteId: cliente.id,
      categoriaId: completos.categoriaId,
      producto: completos.producto.trim(),
      cantidad: completos.cantidad,
      modalidad: completos.modalidad,
      esUrgente: completos.esUrgente ?? false,
      ubicacion: completos.ubicacion,
      radioEnMetros: RADIO_INICIAL_EN_METROS,
      estado: 'ABIERTA',
      fechaPublicacion: new Date(ahora).toISOString(),
      fechaVencimiento: new Date(ahora + TREINTA_MINUTOS_MS).toISOString(),
      tiendasNotificadas,
    }
    base.solicitudes.push(solicitud)
    return HttpResponse.json(aRespuesta(solicitud), {
      status: 201,
      headers: { Location: `${RUTA}/${solicitud.id}` },
    })
  }),
]
