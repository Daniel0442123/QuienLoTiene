import { createBrowserRouter, Outlet, type RouteObject } from 'react-router'
import { ErrorDeRuta } from './ErrorDeRuta'
import { Plantilla } from './Plantilla'
import { RutaProtegida } from './RutaProtegida'

/**
 * Rutas de la PWA, en español y kebab-case (DECISIONES-FRONTEND sección 5).
 * Cada página se carga con `lazy`: un cliente nunca descarga el panel de la tienda ni el del administrador (F-16).
 */
export const rutas: RouteObject[] = [
  {
    element: <Plantilla />,
    errorElement: <ErrorDeRuta />,
    children: [
      {
        errorElement: <ErrorDeRuta />,
        children: [
          {
            index: true,
            lazy: async () => ({ Component: (await import('@/pages/InicioPagina')).InicioPagina }),
          },
          {
            path: 'entrar',
            lazy: async () => ({ Component: (await import('@/pages/InicioSesionPagina')).InicioSesionPagina }),
          },
          {
            path: 'entrar/codigo',
            lazy: async () => ({ Component: (await import('@/pages/SegundoFactorPagina')).SegundoFactorPagina }),
          },
          {
            // Administración (HU-036): solo el rol ADMIN, que entra con segundo factor (HU-004)
            path: 'admin',
            element: (
              <RutaProtegida roles={['ADMIN']}>
                <Outlet />
              </RutaProtegida>
            ),
            children: [
              {
                path: 'tiendas',
                lazy: async () => ({ Component: (await import('@/pages/RevisarTiendasPagina')).RevisarTiendasPagina }),
              },
            ],
          },
          {
            path: 'crear-cuenta',
            lazy: async () => ({
              Component: (await import('@/pages/RegistroClientePagina')).RegistroClientePagina,
            }),
          },
          {
            path: 'registrar-tienda',
            lazy: async () => ({ Component: (await import('@/pages/RegistroTiendaPagina')).RegistroTiendaPagina }),
          },
          {
            path: '*',
            lazy: async () => ({ Component: (await import('@/pages/NoEncontradaPagina')).NoEncontradaPagina }),
          },
        ],
      },
    ],
  },
]

export const enrutador = createBrowserRouter(rutas)
