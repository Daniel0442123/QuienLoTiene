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
              {
                // Reportes y suspender tiendas (HU-037)
                path: 'reportes',
                lazy: async () => ({ Component: (await import('@/pages/ReportesAdminPagina')).ReportesAdminPagina }),
              },
              {
                // Categorías y productos restringidos (HU-038)
                path: 'catalogo',
                lazy: async () => ({ Component: (await import('@/pages/CatalogoAdminPagina')).CatalogoAdminPagina }),
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
            // Perfil y direcciones del cliente (HU-007)
            path: 'cuenta',
            element: (
              <RutaProtegida roles={['CLIENTE']}>
                <Outlet />
              </RutaProtegida>
            ),
            children: [
              {
                index: true,
                lazy: async () => ({ Component: (await import('@/pages/MiCuentaPagina')).MiCuentaPagina }),
              },
            ],
          },
          {
            // Publicar solicitud (HU-016) y ver sus respuestas (HU-020, HU-021): solo el rol CLIENTE
            path: 'solicitudes',
            element: (
              <RutaProtegida roles={['CLIENTE']}>
                <Outlet />
              </RutaProtegida>
            ),
            children: [
              {
                // Historial de preguntas y «Repetir» (HU-008, SC-018)
                index: true,
                lazy: async () => ({
                  Component: (await import('@/pages/HistorialSolicitudesPagina')).HistorialSolicitudesPagina,
                }),
              },
              {
                path: 'nueva',
                lazy: async () => ({
                  Component: (await import('@/pages/PublicarSolicitudPagina')).PublicarSolicitudPagina,
                }),
              },
              {
                // Respuestas de mi solicitud y su estado (HU-020, HU-021)
                path: ':solicitudId',
                lazy: async () => ({
                  Component: (await import('@/pages/SolicitudClientePagina')).SolicitudClientePagina,
                }),
              },
              {
                // Elegir una respuesta y confirmar el pedido (HU-024)
                path: ':solicitudId/elegir/:respuestaId',
                lazy: async () => ({
                  Component: (await import('@/pages/ConfirmarPedidoPagina')).ConfirmarPedidoPagina,
                }),
              },
            ],
          },
          {
            // Pedidos del cliente (HU-024, HU-027, HU-028)
            path: 'pedidos',
            element: (
              <RutaProtegida roles={['CLIENTE']}>
                <Outlet />
              </RutaProtegida>
            ),
            children: [
              {
                index: true,
                lazy: async () => ({ Component: (await import('@/pages/MisPedidosPagina')).MisPedidosPagina }),
              },
              {
                path: ':ordenId',
                lazy: async () => ({ Component: (await import('@/pages/PedidoPagina')).PedidoPagina }),
              },
              {
                // Chat del pedido (HU-033)
                path: ':ordenId/chat',
                lazy: async () => ({ Component: (await import('@/pages/ChatPedidoPagina')).ChatPedidoClientePagina }),
              },
              {
                // Reportar un problema del pedido (HU-035)
                path: ':ordenId/reportar',
                lazy: async () => ({
                  Component: (await import('@/pages/ReportarProblemaPagina')).ReportarProblemaClientePagina,
                }),
              },
              {
                // Calificar la tienda de un pedido resuelto (HU-034)
                path: ':ordenId/calificar',
                lazy: async () => ({
                  Component: (await import('@/pages/CalificarPedidoPagina')).CalificarPedidoPagina,
                }),
              },
            ],
          },
          {
            // Detalle público de una tienda (HU-014): para el cliente que compara respuestas o sigue su pedido
            path: 'tiendas/:tiendaId',
            element: (
              <RutaProtegida roles={['CLIENTE']}>
                <Outlet />
              </RutaProtegida>
            ),
            children: [
              {
                index: true,
                lazy: async () => ({ Component: (await import('@/pages/DetalleTiendaPagina')).DetalleTiendaPagina }),
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
                // Solicitudes nuevas (HU-018), responder (HU-019) y «Mis respuestas» (HU-021)
                path: 'solicitudes',
                children: [
                  {
                    index: true,
                    lazy: async () => ({
                      Component: (await import('@/pages/SolicitudesTiendaPagina')).SolicitudesTiendaPagina,
                    }),
                  },
                  {
                    path: 'mis-respuestas',
                    lazy: async () => ({
                      Component: (await import('@/pages/MisRespuestasTiendaPagina')).MisRespuestasTiendaPagina,
                    }),
                  },
                  {
                    path: ':solicitudId',
                    lazy: async () => ({
                      Component: (await import('@/pages/ResponderSolicitudPagina')).ResponderSolicitudPagina,
                    }),
                  },
                ],
              },
              {
                // Pedidos de la tienda (HU-027, HU-032)
                path: 'pedidos',
                children: [
                  {
                    index: true,
                    lazy: async () => ({
                      Component: (await import('@/pages/PedidosTiendaPagina')).PedidosTiendaPagina,
                    }),
                  },
                  {
                    path: ':ordenId',
                    lazy: async () => ({
                      Component: (await import('@/pages/PedidoTiendaPagina')).PedidoTiendaPagina,
                    }),
                  },
                  {
                    // Chat del pedido (HU-033)
                    path: ':ordenId/chat',
                    lazy: async () => ({
                      Component: (await import('@/pages/ChatPedidoPagina')).ChatPedidoTiendaPagina,
                    }),
                  },
                  {
                    // Reportar un problema del pedido (HU-035)
                    path: ':ordenId/reportar',
                    lazy: async () => ({
                      Component: (await import('@/pages/ReportarProblemaPagina')).ReportarProblemaTiendaPagina,
                    }),
                  },
                ],
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
