import { useQuery } from '@tanstack/react-query'
import { clienteApi } from '@/services/clienteApi'

/** Clave de la lista: las mutaciones de features/clientes la invalidan para que todas las pantallas la vean al día. */
export const CLAVE_DIRECCIONES = ['clientes', 'me', 'direcciones'] as const

/**
 * Direcciones guardadas del cliente (HU-007). Compartido: publicar solicitud (HU-016) las ofrece para no pedir la
 * ubicación otra vez (WCAG 3.3.7) y el perfil del cliente las administra.
 */
export function useDirecciones() {
  return useQuery({
    queryKey: CLAVE_DIRECCIONES,
    queryFn: async () => (await clienteApi.GET('/api/v1/clientes/me/direcciones')).data ?? [],
  })
}
