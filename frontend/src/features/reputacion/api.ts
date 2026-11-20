import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { clienteApi } from '@/services/clienteApi'
import { esErrorApi } from '@/services/errores'
import type { components } from '@/services/api-tipos'

/** Consultas y mutaciones de reputación: reseñas de órdenes y de tiendas (HU-034, HU-014). */

type Esquemas = components['schemas']

export type Resena = Esquemas['ResenaResponse']
export type ReputacionTienda = Esquemas['ReputacionTiendaResponse']
export type ResenaPublica = Esquemas['ResenaPublica']

export const clavesReputacion = {
  // Bajo ['ordenes', id]: un cambio de la orden (SSE) también refresca su reseña
  resenaDeOrden: (ordenId: string) => ['ordenes', ordenId, 'resena'] as const,
  deTienda: (tiendaId: string) => ['tiendas', 'publica', tiendaId, 'resenas'] as const,
}

function exigirDatos<T>(data: T | undefined): T {
  if (data === undefined) throw new Error('La API respondió sin datos')
  return data
}

/** La reseña de una orden, o `null` si todavía no tiene (404 REP-003 no es un error para la pantalla). */
export function useResenaDeOrden(ordenId: string, habilitado = true) {
  return useQuery({
    queryKey: clavesReputacion.resenaDeOrden(ordenId),
    queryFn: async (): Promise<Resena | null> => {
      try {
        return exigirDatos(
          (await clienteApi.GET('/api/v1/ordenes/{ordenId}/resena', { params: { path: { ordenId } } })).data,
        )
      } catch (error) {
        if (esErrorApi(error) && error.codigo === 'REP-003') return null
        throw error
      }
    },
    enabled: habilitado,
  })
}

export interface DatosCalificacion {
  estrellas: number
  comentario?: string
}

/**
 * HU-034: calificar la tienda de una orden RESUELTA. Sin actualización optimista: el backend puede rechazar si la
 * orden no está resuelta (REP-001) o ya tiene reseña (REP-002); en ese caso se vuelve a pedir la reseña.
 */
export function useCalificar(ordenId: string) {
  const clienteConsultas = useQueryClient()
  return useMutation({
    mutationFn: async (calificacion: DatosCalificacion) =>
      exigirDatos(
        (
          await clienteApi.POST('/api/v1/ordenes/{ordenId}/resena', {
            params: { path: { ordenId } },
            body: calificacion,
          })
        ).data,
      ),
    onSuccess: (resena) => {
      clienteConsultas.setQueryData(clavesReputacion.resenaDeOrden(ordenId), resena)
      void clienteConsultas.invalidateQueries({ queryKey: clavesReputacion.deTienda(resena.tiendaId) })
    },
    onError: () => void clienteConsultas.invalidateQueries({ queryKey: clavesReputacion.resenaDeOrden(ordenId) }),
  })
}

/** HU-014: promedio (0 sin reseñas), total y las 10 reseñas más recientes de una tienda, sin el nombre del cliente. */
export function useReputacionTienda(tiendaId: string) {
  return useQuery({
    queryKey: clavesReputacion.deTienda(tiendaId),
    queryFn: async () =>
      exigirDatos(
        (await clienteApi.GET('/api/v1/tiendas/{tiendaId}/resenas', { params: { path: { tiendaId } } })).data,
      ),
  })
}
