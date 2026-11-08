import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { RouterProvider } from 'react-router'
import { ProveedorAnuncios } from '@/components/RegionAnuncios'
import { esErrorApi } from '@/services/errores'
import { renovarSesion } from '@/services/sesion'
import { enrutador } from './rutas'

const clienteConsultas = new QueryClient({
  defaultOptions: {
    queries: {
      // Las listas también se actualizan por SSE: no hace falta pedirlas de nuevo a cada rato (F-16)
      staleTime: 30_000,
      // Un 4xx no se arregla reintentando; solo se reintenta si no hubo conexión o el servidor falló
      retry: (intentos, error) => intentos < 2 && (!esErrorApi(error) || error.status === 0 || error.status >= 500),
    },
    mutations: { retry: false },
  },
})

export function Aplicacion() {
  const [sesionRevisada, setSesionRevisada] = useState(false)

  // Al abrir la app se intenta recuperar la sesión con la cookie de refresco (F-06)
  useEffect(() => {
    renovarSesion()
      .catch(() => null)
      .finally(() => setSesionRevisada(true))
  }, [])

  if (!sesionRevisada) {
    return <p className="p-6 text-texto-suave">Cargando…</p>
  }

  return (
    <QueryClientProvider client={clienteConsultas}>
      <ProveedorAnuncios>
        <RouterProvider router={enrutador} />
      </ProveedorAnuncios>
    </QueryClientProvider>
  )
}
