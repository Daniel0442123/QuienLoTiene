import { NavLink } from 'react-router'
import { cn } from '@/components/ui/utilidades'
import { textosAdministracion as textos } from './textos'

const ENLACES = [
  { a: '/admin/tiendas', texto: textos.enlaceTiendas },
  { a: '/admin/catalogo', texto: textos.enlaceCatalogo },
] as const

/**
 * Secciones del administrador (HU-036, HU-038). La actual va marcada con `aria-current="page"` (lo pone NavLink) y
 * con texto en negrita y subrayado, no solo con color.
 */
export function NavegacionAdmin() {
  return (
    <nav aria-label={textos.navegacion}>
      <ul className="flex flex-wrap gap-2">
        {ENLACES.map(({ a, texto }) => (
          <li key={a}>
            <NavLink
              to={a}
              className={({ isActive }) =>
                cn(
                  'inline-flex min-h-tactil items-center rounded-control border px-4',
                  isActive
                    ? 'border-primario bg-superficie font-semibold underline underline-offset-4'
                    : 'border-borde',
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
