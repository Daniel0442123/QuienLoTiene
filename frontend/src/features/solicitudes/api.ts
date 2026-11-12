import { useMutation } from '@tanstack/react-query'
import type { ComponentesProvisionales } from '@/services/api-tipos-provisionales'
import { clienteApi } from '@/services/clienteApi'
import type { DatosPublicarSolicitud } from './esquemas'

/** Consultas y mutaciones de solicitudes (DECISIONES-FRONTEND sección 4). */

export type Solicitud = ComponentesProvisionales['SolicitudResponse']

/**
 * HU-016: publica «¿quién tiene X?» para las tiendas cercanas. Sin actualización optimista: el backend puede rechazar
 * con 429 (SOL-001) y mostrar un éxito falso confundiría (DECISIONES-FRONTEND 2.3).
 */
export function usePublicarSolicitud() {
  return useMutation({
    mutationFn: async ({ ubicacion, ...datos }: DatosPublicarSolicitud) => {
      if (!ubicacion) throw new Error('La solicitud se envió sin ubicación')
      const { data } = await clienteApi.POST('/api/v1/solicitudes', { body: { ...datos, ubicacion } })
      if (!data) throw new Error('La API no devolvió la solicitud publicada')
      return data
    },
  })
}
