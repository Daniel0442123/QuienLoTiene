import { useQuery } from '@tanstack/react-query'
import { clienteApi } from '@/services/clienteApi'

/**
 * Direcciones guardadas del cliente (HU-007). Compartido: publicar solicitud (HU-016) las ofrece para no pedir la
 * ubicación otra vez (WCAG 3.3.7) y el perfil del cliente las administra.
 */
export function useDirecciones() {
  return useQuery({
    queryKey: ['clientes', 'me', 'direcciones'],
    queryFn: async () => (await clienteApi.GET('/api/v1/clientes/me/direcciones')).data ?? [],
  })
}
