import { createBrowserRouter, type RouteObject } from 'react-router'
import { ErrorDeRuta } from './ErrorDeRuta'
import { Plantilla } from './Plantilla'

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
            path: 'crear-cuenta',
            lazy: async () => ({
              Component: (await import('@/pages/RegistroClientePagina')).RegistroClientePagina,
            }),
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
