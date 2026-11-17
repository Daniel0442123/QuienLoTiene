import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { components } from '@/services/api-tipos-sprint-2'
import { clienteApi } from '@/services/clienteApi'
import type { DatosConfirmarPedido } from './esquemas'

/** Consultas y mutaciones de órdenes (DECISIONES-FRONTEND sección 4). */

type Esquemas = components['schemas']

export type Orden = Esquemas['OrdenResponse']
export type EstadoOrden = Esquemas['EstadoOrden']
export type MedioPago = Esquemas['MedioPago']

export const clavesOrdenes = {
  todas: ['ordenes'] as const,
  orden: (ordenId: string) => ['ordenes', ordenId] as const,
}

function exigirDatos<T>(data: T | undefined): T {
  if (data === undefined) throw new Error('La API respondió sin datos')
  return data
}

/**
 * HU-024: elegir una respuesta y crear la orden CONFIRMADO. Sin actualización optimista: el backend puede rechazar por
 * medio de pago (ORD-003), cobertura (TIE-010) o solicitud cerrada (SOL-004). La solicitud queda RESUELTA.
 */
export function useCrearOrden() {
  const clienteConsultas = useQueryClient()
  return useMutation({
    mutationFn: async (pedido: DatosConfirmarPedido & { respuestaId: string }) =>
      exigirDatos((await clienteApi.POST('/api/v1/ordenes', { body: pedido })).data),
    onSuccess: (orden) => {
      clienteConsultas.setQueryData(clavesOrdenes.orden(orden.id), orden)
      void clienteConsultas.invalidateQueries({ queryKey: ['solicitudes', orden.solicitudId] })
      void clienteConsultas.invalidateQueries({ queryKey: clavesOrdenes.todas, exact: true })
    },
  })
}

/** HU-024 y HU-027: una orden del cliente o de la tienda. */
export function useOrden(ordenId: string) {
  return useQuery({
    queryKey: clavesOrdenes.orden(ordenId),
    queryFn: async () =>
      exigirDatos((await clienteApi.GET('/api/v1/ordenes/{ordenId}', { params: { path: { ordenId } } })).data),
  })
}
