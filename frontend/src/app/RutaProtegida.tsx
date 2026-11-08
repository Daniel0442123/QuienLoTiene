import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useSesion } from '@/hooks/useSesion'
import type { Rol } from '@/services/sesion'

/**
 * Lleva al inicio de sesión si no hay sesión, y al inicio si el rol no corresponde.
 * Es solo comodidad para el usuario: la autorización real la hace el backend.
 */
export function RutaProtegida({ roles, children }: { roles: Rol[]; children: ReactNode }) {
  const sesion = useSesion()
  const ubicacion = useLocation()

  if (!sesion) {
    return <Navigate to="/entrar" replace state={{ volverA: ubicacion.pathname }} />
  }
  if (!roles.includes(sesion.rol)) {
    return <Navigate to="/" replace />
  }
  return children
}
