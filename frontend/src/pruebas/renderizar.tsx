import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import type { ReactElement } from 'react'
import { MemoryRouter } from 'react-router'
import { ProveedorAnuncios } from '@/components/RegionAnuncios'

/** Renderiza un componente con los mismos proveedores de la aplicación (consultas, anuncios y rutas). */
export function renderizarConProveedores(componente: ReactElement, { ruta = '/' }: { ruta?: string } = {}) {
  const clienteConsultas = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  return render(
    <QueryClientProvider client={clienteConsultas}>
      <ProveedorAnuncios>
        <MemoryRouter initialEntries={[ruta]}>{componente}</MemoryRouter>
      </ProveedorAnuncios>
    </QueryClientProvider>,
  )
}
