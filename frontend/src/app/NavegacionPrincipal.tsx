import { Link, useLocation } from 'react-router'
import { cn } from '@/components/ui/utilidades'
import { BotonCerrarSesion } from '@/features/identidad/BotonCerrarSesion'
import { textosInicio as textos } from '@/features/inicio/textos'
import type { Rol } from '@/services/sesion'
import { esEnlaceActivo, NAVEGACION_POR_ROL } from './navegacion'

export const ID_MENU_PRINCIPAL = 'menu-principal'

/**
 * Menú principal con ícono y texto por módulo según el rol (SC-020). El texto siempre se ve: un ícono solo no basta
 * para quien no lo reconoce ni para el control por voz.
 *
 * - Escritorio (≥ 1024 px): barra lateral fija a la izquierda.
 * - Celular y tableta: el mismo `<nav>`, oculto hasta que se toca «Menú» en el encabezado. No es una barra inferior
 *   fija porque la pantalla de preguntar y el chat ya usan el borde inferior para su botón y su caja de texto.
 *
 * El módulo actual lleva `aria-current="page"` y se marca con negrita, fondo y borde, no solo con color (WCAG 1.4.1).
 */
export function NavegacionPrincipal({ rol, abierto }: { rol: Rol; abierto: boolean }) {
  const { pathname } = useLocation()
  return (
    <nav
      id={ID_MENU_PRINCIPAL}
      aria-label={textos.navegacionPrincipal}
      className={cn(
        abierto ? 'flex' : 'hidden',
        'flex-col gap-4 border-b border-borde-suave bg-fondo px-4 py-4',
        'lg:sticky lg:top-0 lg:flex lg:max-h-dvh lg:w-64 lg:shrink-0 lg:self-start lg:overflow-y-auto lg:border-r lg:border-b-0 lg:py-6',
      )}
    >
      <ul className="flex flex-col gap-1">
        {NAVEGACION_POR_ROL[rol].map((enlace) => {
          const activo = esEnlaceActivo(enlace, pathname)
          const { Icono } = enlace
          return (
            <li key={enlace.a}>
              <Link
                to={enlace.a}
                aria-current={activo ? 'page' : undefined}
                className={cn(
                  'flex min-h-tactil items-center gap-3 rounded-control border-l-4 px-3 py-2 hover:bg-superficie',
                  activo ? 'border-primario bg-superficie font-semibold text-primario' : 'border-transparent',
                )}
              >
                <Icono aria-hidden="true" className="size-5 shrink-0" />
                <span>{enlace.texto}</span>
              </Link>
            </li>
          )
        })}
      </ul>
      <div className="border-t border-borde-suave pt-4">
        <BotonCerrarSesion />
      </div>
    </nav>
  )
}
