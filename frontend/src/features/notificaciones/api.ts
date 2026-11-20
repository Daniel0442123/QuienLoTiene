import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { clienteApi } from '@/services/clienteApi'
import { consultarEstadoPush, suscribirEsteNavegador } from '@/services/push'

/** Consultas y mutaciones de las notificaciones push (HU-031). */

export const clavesPush = {
  configuracion: ['notificaciones', 'configuracion-push'] as const,
  // Estado de este navegador (permiso y suscripción), no de la API
  estado: ['notificaciones', 'estado-push'] as const,
}

/** `{ habilitado, clavePublica }`: con habilitado false no se ofrece activar ni se pide el permiso. */
export function useConfiguracionPush(habilitada = true) {
  return useQuery({
    queryKey: clavesPush.configuracion,
    queryFn: async () => {
      const { data } = await clienteApi.GET('/api/v1/notificaciones/configuracion-push')
      if (!data) throw new Error('La API respondió sin datos')
      return data
    },
    enabled: habilitada,
    staleTime: 60 * 60_000,
  })
}

export function useEstadoPush(habilitada = true) {
  return useQuery({ queryKey: clavesPush.estado, queryFn: consultarEstadoPush, enabled: habilitada })
}

/**
 * Activar: pide el permiso (después del toque), suscribe este navegador y manda `subscription.toJSON()`. Si el
 * servidor la rechaza (NOT-001), se da de baja también en el navegador para no quedar a medias.
 */
export function useActivarPush() {
  const clienteConsultas = useQueryClient()
  return useMutation({
    mutationFn: async (clavePublica: string): Promise<'activo' | 'bloqueado'> => {
      const suscripcion = await suscribirEsteNavegador(clavePublica)
      if (suscripcion === 'bloqueado') return 'bloqueado'
      const { endpoint, keys } = suscripcion.toJSON()
      try {
        await clienteApi.POST('/api/v1/notificaciones/suscripciones', {
          body: { endpoint: endpoint ?? '', keys: { p256dh: keys?.p256dh ?? '', auth: keys?.auth ?? '' } },
        })
      } catch (error) {
        await suscripcion.unsubscribe()
        throw error
      }
      return 'activo'
    },
    onSettled: () => void clienteConsultas.invalidateQueries({ queryKey: clavesPush.estado }),
  })
}

/** Desactivar: borra la suscripción en el servidor (204 aunque no exista) y en el navegador. */
export function useDesactivarPush() {
  const clienteConsultas = useQueryClient()
  return useMutation({
    mutationFn: async (suscripcion: PushSubscription) => {
      await clienteApi.DELETE('/api/v1/notificaciones/suscripciones', {
        params: { query: { endpoint: suscripcion.endpoint } },
      })
      await suscripcion.unsubscribe()
    },
    onSettled: () => void clienteConsultas.invalidateQueries({ queryKey: clavesPush.estado }),
  })
}
