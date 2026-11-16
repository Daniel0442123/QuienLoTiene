import {
  base,
  TIENDAS_SPRINT_2,
  type EstadoOrdenSimulado,
  type OrdenSimulada,
  type RespuestaSimulada,
  type SolicitudSimulada,
  type TiendaSimulada,
} from './datos'

/**
 * Reglas del contrato del Sprint 2 (docs/api/CONTRATO-SPRINT-2.md) que comparten los manejadores de respuestas y
 * de órdenes. Imitan al backend lo justo para que las pantallas se prueben con los mismos casos.
 */

const RADIO_TIERRA_EN_METROS = 6_371_000

/** Distancia en línea recta (haversine), en metros enteros. */
export function distanciaEnMetros(
  a: { latitud: number; longitud: number },
  b: { latitud: number; longitud: number },
): number {
  const rad = (grados: number) => (grados * Math.PI) / 180
  const dLat = rad(b.latitud - a.latitud)
  const dLon = rad(b.longitud - a.longitud)
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(rad(a.latitud)) * Math.cos(rad(b.latitud)) * Math.sin(dLon / 2) ** 2
  return Math.round(2 * RADIO_TIERRA_EN_METROS * Math.asin(Math.sqrt(h)))
}

/** Distancia que ve la tienda: redondeada a 100 m y mínimo 100 m (decisión 6 del contrato, CP-SOL-018-02). */
export function distanciaAproximada(metros: number): number {
  return Math.max(100, Math.round(metros / 100) * 100)
}

export function ubicacionDeTienda(tienda: TiendaSimulada) {
  return { latitud: tienda.latitud, longitud: tienda.longitud }
}

/** Costo del domicilio de la tienda hasta un punto, o `null` si no llega (TIE-010). */
export function costoDomicilio(tienda: TiendaSimulada, destino: { latitud: number; longitud: number }) {
  const { haceDomicilio, radioCoberturaEnMetros, tarifaFijaEnPesos } = tienda.domicilio
  if (!haceDomicilio || radioCoberturaEnMetros === null) return null
  if (distanciaEnMetros(ubicacionDeTienda(tienda), destino) > radioCoberturaEnMetros) return null
  return tarifaFijaEnPesos ?? 0
}

/** Las solicitudes ABIERTA que ya pasaron su vencimiento quedan VENCIDA (HU-021, sin temporizadores). */
export function actualizarVencimientos(ahora = Date.now()): void {
  for (const solicitud of base.solicitudes) {
    if (solicitud.estado === 'ABIERTA' && Date.parse(solicitud.fechaVencimiento) <= ahora) solicitud.estado = 'VENCIDA'
  }
}

/** Las respuestas que ya existen: las de las tiendas simuladas tienen hora futura hasta que «llegan». */
export function respuestasVisibles(solicitudId: string, ahora = Date.now()): RespuestaSimulada[] {
  return base.respuestas.filter((r) => r.solicitudId === solicitudId && Date.parse(r.fechaRespuesta) <= ahora)
}

/** La solicitud llegó a la tienda: misma categoría y la tienda dentro del radio de la solicitud. */
export function llegoALaTienda(solicitud: SolicitudSimulada, tienda: TiendaSimulada): boolean {
  return (
    tienda.estado === 'APROBADA' &&
    solicitud.categoriaId === tienda.categoriaId &&
    distanciaEnMetros(solicitud.ubicacion, ubicacionDeTienda(tienda)) <= solicitud.radioEnMetros
  )
}

/** SolicitudResponse: la solicitud como la ve su cliente (sin `clienteId`). */
export function aSolicitudResponse(solicitud: SolicitudSimulada) {
  const { clienteId: _clienteId, ...datos } = solicitud
  return { ...datos, respuestasRecibidas: respuestasVisibles(solicitud.id).length }
}

/** Mismo producto: mismo texto sin importar mayúsculas ni espacios (SC-011). */
export function normalizarProducto(producto: string): string {
  return producto.toLowerCase().replace(/\s+/g, ' ').trim()
}

/** Estado de la respuesta para la tienda: se calcula, no se guarda (decisión 7 del contrato). */
export function estadoDeRespuesta(respuesta: RespuestaSimulada, solicitud: SolicitudSimulada) {
  if (solicitud.estado === 'ABIERTA') return 'ESPERANDO' as const
  if (solicitud.estado === 'VENCIDA') return 'VENCIDA' as const
  const orden = base.ordenes.find((o) => o.id === solicitud.ordenId)
  return orden?.respuestaId === respuesta.id ? ('ELEGIDA' as const) : ('NO_ELEGIDA' as const)
}

const ESTADOS_ACTIVOS: EstadoOrdenSimulado[] = [
  'CONFIRMADO',
  'PREPARANDO',
  'EN_CAMINO',
  'LISTO_PARA_RECOGER',
  'ENTREGADO',
]

export function ordenActiva(orden: OrdenSimulada): boolean {
  return ESTADOS_ACTIVOS.includes(orden.estado)
}

/** Paso siguiente válido (diagrama 05). La tienda no puede saltar estados ni cerrar la orden. */
export function siguienteEstadoValido(orden: OrdenSimulada, nuevo: EstadoOrdenSimulado): boolean {
  switch (orden.estado) {
    case 'CONFIRMADO':
      return nuevo === 'PREPARANDO'
    case 'PREPARANDO':
      return nuevo === (orden.modalidad === 'DOMICILIO' ? 'EN_CAMINO' : 'LISTO_PARA_RECOGER')
    case 'EN_CAMINO':
    case 'LISTO_PARA_RECOGER':
      return nuevo === 'ENTREGADO'
    default:
      return false
  }
}

/**
 * Las dos tiendas simuladas responden a una solicitud nueva que les llegue: Botica San José dice «Sí lo tengo» a
 * los 6 s y Farmacia Popular «Tengo algo parecido» a los 15 s. Así HU-020 se ve llegar en pantalla.
 */
export function respuestasAutomaticas(solicitud: SolicitudSimulada, ahora = Date.now()): RespuestaSimulada[] {
  const plan = [
    {
      tiendaId: TIENDAS_SPRINT_2.botica,
      segundos: 6,
      tipo: 'SI' as const,
      precioEnPesos: 2500,
      descripcionAlternativa: null,
      tiempoEstimadoEnMinutos: 20,
    },
    {
      tiendaId: TIENDAS_SPRINT_2.popular,
      segundos: 15,
      tipo: 'PARECIDO' as const,
      precioEnPesos: 1800,
      descripcionAlternativa: `${solicitud.producto}, marca genérica`,
      tiempoEstimadoEnMinutos: null,
    },
  ]
  return plan.flatMap(({ tiendaId, segundos, ...datos }) => {
    const tienda = base.tiendas.find((t) => t.id === tiendaId)
    if (!tienda?.estaAbierta || !llegoALaTienda(solicitud, tienda)) return []
    const costo = costoDomicilio(tienda, solicitud.ubicacion)
    return [
      {
        id: crypto.randomUUID(),
        solicitudId: solicitud.id,
        tiendaId,
        ...datos,
        cantidadDisponible: solicitud.cantidad,
        ofreceDomicilio: costo !== null,
        costoDomicilioEnPesos: costo,
        fechaRespuesta: new Date(ahora + segundos * 1000).toISOString(),
      },
    ]
  })
}
