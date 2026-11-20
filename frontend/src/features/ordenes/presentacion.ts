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

/** Quién mira la orden: cambia qué puede hacer y cómo se le habla. */
export type VistaOrden = 'cliente' | 'tienda'

/**
 * HU-029 (decisión 1 del contrato): el cliente cancela mientras no salió de la tienda; la tienda, solo antes de
 * empezar a preparar. Fuera de eso no se ofrece el botón (mockup C-15: «en camino» sin «Cancelar»).
 */
export function puedeCancelar(vista: VistaOrden, estado: EstadoOrden): boolean {
  if (vista === 'tienda') return estado === 'CONFIRMADO'
  return estado === 'CONFIRMADO' || estado === 'PREPARANDO' || estado === 'LISTO_PARA_RECOGER'
}

/**
 * HU-026: enlace de Google Maps con la ruta hasta la tienda. En el celular abre la app de mapas y en el computador
 * la página. Solo lleva la ubicación de la tienda, ningún dato del cliente.
 */
export function enlaceComoLlegar(latitud: number, longitud: number): string {
  return `https://www.google.com/maps/dir/?api=1&destination=${latitud},${longitud}`
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
