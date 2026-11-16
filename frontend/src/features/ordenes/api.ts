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

/** Mientras no llega el tiempo real (HU-030), una orden activa se vuelve a pedir cada 15 s. */
const INTERVALO_ORDEN_ACTIVA_MS = 15_000

/** HU-024 y HU-027: una orden del cliente o de la tienda. */
export function useOrden(ordenId: string) {
  return useQuery({
    queryKey: clavesOrdenes.orden(ordenId),
    queryFn: async () =>
      exigirDatos((await clienteApi.GET('/api/v1/ordenes/{ordenId}', { params: { path: { ordenId } } })).data),
    refetchInterval: (consulta) => (consulta.state.data?.esActiva ? INTERVALO_ORDEN_ACTIVA_MS : false),
  })
}

/** HU-027: mis órdenes (cliente) o las recibidas (tienda), activas primero; lo ordena la API. */
export function useOrdenes() {
  return useQuery({
    queryKey: clavesOrdenes.todas,
    queryFn: async () => exigirDatos((await clienteApi.GET('/api/v1/ordenes')).data),
    refetchInterval: INTERVALO_ORDEN_ACTIVA_MS,
  })
}

function useGuardarOrden() {
  const clienteConsultas = useQueryClient()
  return (orden: Orden) => {
    clienteConsultas.setQueryData(clavesOrdenes.orden(orden.id), orden)
    void clienteConsultas.invalidateQueries({ queryKey: clavesOrdenes.todas, exact: true })
  }
}

export type EstadoQueAvanzaLaTienda = Esquemas['CambiarEstadoOrdenRequest']['estado']

/**
 * HU-027: la tienda avanza la orden al paso siguiente. Sin actualización optimista: si otro celular ya la avanzó,
 * el backend responde ORD-002 y se vuelve a pedir la orden para mostrar el estado real.
 */
export function useCambiarEstadoOrden(ordenId: string) {
  const clienteConsultas = useQueryClient()
  const guardar = useGuardarOrden()
  return useMutation({
    mutationFn: async (estado: EstadoQueAvanzaLaTienda) =>
      exigirDatos(
        (
          await clienteApi.PATCH('/api/v1/ordenes/{ordenId}/estado', {
            params: { path: { ordenId } },
            body: { estado },
          })
        ).data,
      ),
    onSuccess: guardar,
    onError: () => void clienteConsultas.invalidateQueries({ queryKey: clavesOrdenes.orden(ordenId) }),
  })
}

/** HU-028: «Recibí mi pedido». La orden queda RESUELTA y la tienda deja de ver los datos del cliente. */
export function useConfirmarRecepcion(ordenId: string) {
  const guardar = useGuardarOrden()
  return useMutation({
    mutationFn: async () =>
      exigirDatos(
        (await clienteApi.POST('/api/v1/ordenes/{ordenId}/recepcion', { params: { path: { ordenId } } })).data,
      ),
    onSuccess: guardar,
  })
}

/** HU-032: enlace wa.me de la otra parte; solo se pide con la orden activa (MEN-001 si no). */
export function useContactoWhatsapp(ordenId: string, habilitado: boolean) {
  return useQuery({
    queryKey: [...clavesOrdenes.orden(ordenId), 'whatsapp'],
    queryFn: async () =>
      exigirDatos((await clienteApi.GET('/api/v1/ordenes/{ordenId}/whatsapp', { params: { path: { ordenId } } })).data),
    enabled: habilitado,
    staleTime: Infinity,
  })
}
