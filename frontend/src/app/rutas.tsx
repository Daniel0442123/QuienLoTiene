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
            // Publicar solicitud (HU-016): solo el rol CLIENTE
            path: 'solicitudes',
            element: (
              <RutaProtegida roles={['CLIENTE']}>
                <Outlet />
              </RutaProtegida>
            ),
            children: [
              {
                path: 'nueva',
                lazy: async () => ({
                  Component: (await import('@/pages/PublicarSolicitudPagina')).PublicarSolicitudPagina,
                }),
              },
            ],
          },
          {
            path: 'registrar-tienda',
            lazy: async () => ({ Component: (await import('@/pages/RegistroTiendaPagina')).RegistroTiendaPagina }),
          },
          {
            // Panel de la tienda (HU-009 a HU-013): solo el rol TIENDA; un paquete aparte que el cliente no descarga
            path: 'tienda',
            element: (
              <RutaProtegida roles={['TIENDA']}>
                <Outlet />
              </RutaProtegida>
            ),
            children: [
              {
                index: true,
                lazy: async () => ({ Component: (await import('@/pages/PanelTiendaPagina')).PanelTiendaPagina }),
              },
              {
                path: 'perfil',
                lazy: async () => ({ Component: (await import('@/pages/PerfilTiendaPagina')).PerfilTiendaPagina }),
              },
              {
                path: 'domicilio',
                lazy: async () => ({
                  Component: (await import('@/pages/DomicilioTiendaPagina')).DomicilioTiendaPagina,
                }),
              },
              {
                path: 'medios-pago',
                lazy: async () => ({
                  Component: (await import('@/pages/MediosPagoTiendaPagina')).MediosPagoTiendaPagina,
                }),
              },
            ],
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
