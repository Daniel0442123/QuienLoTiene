import { Menu, X } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Link, Outlet, useLocation } from 'react-router'
import { cn } from '@/components/ui/utilidades'
import { textosInicio } from '@/features/inicio/textos'
import { ConexionTiempoReal } from '@/features/notificaciones/ConexionTiempoReal'
import { useSesion } from '@/hooks/useSesion'
import { AnunciadorDeRuta } from './AnunciadorDeRuta'
import { AvisoActualizacion } from './AvisoActualizacion'
import { ID_MENU_PRINCIPAL, NavegacionPrincipal } from './NavegacionPrincipal'

/**
 * Estructura común de todas las pantallas: enlace para saltar al contenido, encabezado, menú principal por rol (SC-020)
 * y región principal. Sin sesión no hay menú: el inicio ya ofrece entrar, crear cuenta y registrar la tienda.
 */
export function Plantilla() {
  const sesion = useSesion()
  const { pathname } = useLocation()
  // En celular el menú se cierra al llegar a otra pantalla: se recuerda en qué ruta se abrió
  const [menuAbiertoEn, setMenuAbiertoEn] = useState<string | null>(null)
  const menuAbierto = menuAbiertoEn === pathname
  const botonMenu = useRef<HTMLButtonElement>(null)

  // Escape cierra el menú y devuelve el foco al botón que lo abrió
  useEffect(() => {
    if (!menuAbierto) return
    const alPresionarTecla = (evento: KeyboardEvent) => {
      if (evento.key === 'Escape') {
        setMenuAbiertoEn(null)
        botonMenu.current?.focus()
      }
    }
    document.addEventListener('keydown', alPresionarTecla)
    return () => document.removeEventListener('keydown', alPresionarTecla)
  }, [menuAbierto])

  return (
    <>
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-50 focus:rounded-control focus:bg-fondo focus:p-3"
      >
        Ir al contenido
      </a>
      <header className="border-b border-borde-suave">
        <div
          className={cn('flex items-center justify-between gap-2 px-4 py-3', sesion ? 'lg:px-6' : 'mx-auto max-w-3xl')}
        >
          <Link to="/" className="flex min-h-tactil items-center text-lg font-bold">
            ¿Quién lo tiene?
          </Link>
          {sesion ? (
            <button
              ref={botonMenu}
              type="button"
              aria-expanded={menuAbierto}
              aria-controls={ID_MENU_PRINCIPAL}
              onClick={() => setMenuAbiertoEn(menuAbierto ? null : pathname)}
              className="inline-flex min-h-tactil min-w-tactil items-center justify-center gap-2 rounded-control border border-borde px-3 font-semibold hover:bg-superficie lg:hidden"
            >
              {menuAbierto ? (
                <X aria-hidden="true" className="size-5" />
              ) : (
                <Menu aria-hidden="true" className="size-5" />
              )}
              {textosInicio.botonAbrirMenu}
            </button>
          ) : null}
        </div>
      </header>
      <div className={sesion ? 'lg:flex' : undefined}>
        {sesion ? <NavegacionPrincipal rol={sesion.rol} abierto={menuAbierto} /> : null}
        <main id="contenido" className="min-w-0 flex-1 px-4 py-6 lg:px-8">
          <div className="mx-auto max-w-3xl">
            <Outlet />
          </div>
        </main>
      </div>
      <ConexionTiempoReal />
      <AnunciadorDeRuta />
      <AvisoActualizacion />
    </>
  )
}
