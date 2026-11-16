import type { EstadoOrden, EstadoQueAvanzaLaTienda, Orden } from './api'

/** Número corto de la orden, como lo muestra el backend en WhatsApp: los primeros 8 caracteres en mayúsculas. */
export function numeroCorto(ordenId: string): string {
  return ordenId.slice(0, 8).toUpperCase()
}

type Modalidad = Orden['modalidad']

/** Pasos del pedido según cómo se entrega (diagrama 05). RESUELTA es «Recibido»: lo confirma el cliente (HU-028). */
export function pasosDelPedido(modalidad: Modalidad): EstadoOrden[] {
  return [
    'CONFIRMADO',
    'PREPARANDO',
    modalidad === 'DOMICILIO' ? 'EN_CAMINO' : 'LISTO_PARA_RECOGER',
    'ENTREGADO',
    'RESUELTA',
  ]
}

/** El único paso que puede dar la tienda desde el estado actual, o `null` si no le toca (CP-ORD-027-02). */
export function siguientePasoDeLaTienda(estado: EstadoOrden, modalidad: Modalidad): EstadoQueAvanzaLaTienda | null {
  switch (estado) {
    case 'CONFIRMADO':
      return 'PREPARANDO'
    case 'PREPARANDO':
      return modalidad === 'DOMICILIO' ? 'EN_CAMINO' : 'LISTO_PARA_RECOGER'
    case 'EN_CAMINO':
    case 'LISTO_PARA_RECOGER':
      return 'ENTREGADO'
    default:
      return null
  }
}
