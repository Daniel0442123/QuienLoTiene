import { Link } from 'react-router'
import { textosIdentidad } from '@/features/identidad/textos'
import { textosTiendas } from '@/features/tiendas/textos'
import { useSesion } from '@/hooks/useSesion'

export function InicioPagina() {
  const sesion = useSesion()
  return (
    <section className="flex flex-col gap-4">
      <h1 className="text-2xl font-bold">¿Quién lo tiene?</h1>
      <p>Pregunta una vez y las farmacias y ferreterías cercanas te responden si tienen lo que buscas.</p>
      {sesion ? null : (
        <div className="flex flex-wrap gap-3">
          <Link
            to="/crear-cuenta"
            className="inline-flex min-h-tactil items-center rounded-control bg-primario px-4 font-semibold text-primario-texto"
          >
            {textosIdentidad.enlaceCrearCuenta}
          </Link>
          <Link
            to="/entrar"
            className="inline-flex min-h-tactil items-center rounded-control border border-borde px-4 font-semibold"
          >
            {textosIdentidad.enlaceEntrar}
          </Link>
        </div>
      )}
      {sesion ? null : (
        <p>
          {textosTiendas.invitacionInicio}{' '}
          <Link to="/registrar-tienda" className="text-primario underline underline-offset-4 hover:no-underline">
            {textosTiendas.enlaceRegistrarTienda}
          </Link>
        </p>
      )}
    </section>
  )
}
