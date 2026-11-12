import { useQuery } from '@tanstack/react-query'
import { clienteApi } from '@/services/clienteApi'

/**
 * Categorías de tienda (Farmacia, Ferretería...). Compartido: lo usan el registro de tienda (HU-002) y publicar
 * solicitud (HU-016). El endpoint es una PROPUESTA del frontend mientras el módulo catalogo no exista.
 */
export function useCategorias() {
  return useQuery({
    queryKey: ['categorias'],
    queryFn: async () => {
      const { data } = await clienteApi.GET('/api/v1/categorias')
      return data ?? []
    },
    // Cambian muy poco: no vale la pena pedirlas de nuevo en cada pantalla
    staleTime: 60 * 60 * 1000,
  })
}
