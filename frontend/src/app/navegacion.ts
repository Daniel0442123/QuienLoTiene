import {
  CircleUserRound,
  Clock,
  Flag,
  History,
  House,
  Inbox,
  Package,
  Search,
  Store,
  Tags,
  Truck,
  Wallet,
  type LucideIcon,
} from 'lucide-react'
import { textosAdministracion } from '@/features/administracion/textos'
import { textosClientes } from '@/features/clientes/textos'
import { textosInicio } from '@/features/inicio/textos'
import { textosOrdenes } from '@/features/ordenes/textos'
import { textosSolicitudes } from '@/features/solicitudes/textos'
import { textosTiendas } from '@/features/tiendas/textos'
import type { Rol } from '@/services/sesion'

export interface EnlaceNavegacion {
  a: string
  texto: string
  Icono: LucideIcon
  /** Si la ruta actual pertenece a esta sección; por defecto, la ruta o cualquiera debajo de ella. */
  activo?: (ruta: string) => boolean
}

const dentroDe = (base: string) => (ruta: string) => ruta === base || ruta.startsWith(`${base}/`)
const exacta = (base: string) => (ruta: string) => ruta === base

/**
 * Módulos del menú principal según el rol del token (SC-020). Son las mismas secciones que protege `RutaProtegida` en
 * rutas.tsx: si se agrega una ruta protegida con su propio módulo, se agrega aquí también.
 */
export const NAVEGACION_POR_ROL: Record<Rol, EnlaceNavegacion[]> = {
  CLIENTE: [
    { a: '/', texto: textosInicio.enlaceInicio, Icono: House, activo: exacta('/') },
    { a: '/solicitudes/nueva', texto: textosInicio.enlacePreguntar, Icono: Search },
    {
      a: '/solicitudes',
      texto: textosInicio.enlaceMisPreguntas,
      Icono: History,
      // «Preguntar» vive debajo de /solicitudes, pero es su propio módulo
      activo: (ruta) => dentroDe('/solicitudes')(ruta) && !dentroDe('/solicitudes/nueva')(ruta),
    },
    { a: '/pedidos', texto: textosOrdenes.enlaceMisPedidos, Icono: Package },
    { a: '/cuenta', texto: textosClientes.enlaceMiCuenta, Icono: CircleUserRound },
  ],
  TIENDA: [
    { a: '/', texto: textosInicio.enlaceInicio, Icono: House, activo: exacta('/') },
    { a: '/tienda', texto: textosTiendas.tituloPanel, Icono: Store, activo: exacta('/tienda') },
    { a: '/tienda/solicitudes', texto: textosSolicitudes.seccionSolicitudes, Icono: Inbox },
    { a: '/tienda/pedidos', texto: textosOrdenes.seccionPedidos, Icono: Package },
    { a: '/tienda/perfil', texto: textosTiendas.seccionPerfil, Icono: Clock },
    { a: '/tienda/domicilio', texto: textosTiendas.seccionDomicilio, Icono: Truck },
    { a: '/tienda/medios-pago', texto: textosTiendas.seccionMediosPago, Icono: Wallet },
  ],
  ADMIN: [
    { a: '/', texto: textosInicio.enlaceInicio, Icono: House, activo: exacta('/') },
    { a: '/admin/tiendas', texto: textosAdministracion.enlaceTiendas, Icono: Store },
    { a: '/admin/reportes', texto: textosAdministracion.enlaceReportes, Icono: Flag },
    { a: '/admin/catalogo', texto: textosAdministracion.enlaceCatalogo, Icono: Tags },
  ],
}

export function esEnlaceActivo(enlace: EnlaceNavegacion, ruta: string): boolean {
  return (enlace.activo ?? dentroDe(enlace.a))(ruta)
}
