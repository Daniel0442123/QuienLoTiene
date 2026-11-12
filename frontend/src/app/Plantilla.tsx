import { Link, Outlet } from 'react-router'
import { BotonCerrarSesion } from '@/features/identidad/BotonCerrarSesion'
import { useSesion } from '@/hooks/useSesion'
import { AnunciadorDeRuta } from './AnunciadorDeRuta'
import { AvisoActualizacion } from './AvisoActualizacion'

/** Estructura común de todas las pantallas: enlace para saltar al contenido, encabezado y región principal. */
export function Plantilla() {
  const sesion = useSesion()
  return (
    <>
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-50 focus:rounded-control focus:bg-fondo focus:p-3"
      >
        Ir al contenido
      </a>
      <header className="border-b border-borde-suave">
        <div className="mx-auto flex max-w-3xl items-center justify-between px-4 py-3">
          <Link to="/" className="flex min-h-tactil items-center text-lg font-bold">
            ¿Quién lo tiene?
          </Link>
          {sesion ? <BotonCerrarSesion /> : null}
        </div>
      </header>
      <main id="contenido" className="mx-auto max-w-3xl px-4 py-6">
        <Outlet />
      </main>
      <AnunciadorDeRuta />
      <AvisoActualizacion />
    </>
  )
}
