import { NavLink } from 'react-router'
import { cn } from '@/components/ui/utilidades'
import { textosSolicitudes as textos } from './textos'

const PESTANAS = [
  { ruta: '/tienda/solicitudes', texto: textos.pestanaNuevas },
  { ruta: '/tienda/solicitudes/mis-respuestas', texto: textos.pestanaMisRespuestas },
]

/**
 * «Nuevas» y «Mis respuestas» (SC-011, decisión 4). Son enlaces a dos rutas, no un tablist: cada una es una pantalla
 * con su propia URL y NavLink marca la actual con aria-current="page" (no solo con color).
 */
export function PestanasSolicitudesTienda() {
  return (
    <nav aria-label={textos.pestanas}>
      <ul className="grid grid-cols-2 gap-2">
        {PESTANAS.map(({ ruta, texto }) => (
          <li key={ruta}>
            <NavLink
              to={ruta}
              end
              className={({ isActive }) =>
                cn(
                  'flex min-h-tactil items-center justify-center rounded-control border px-3 font-semibold',
                  isActive
                    ? 'border-primario bg-primario text-primario-texto'
                    : 'border-borde text-texto hover:bg-superficie',
                )
              }
            >
              {texto}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  )
}
