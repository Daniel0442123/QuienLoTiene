import { useSyncExternalStore } from 'react'
import { obtenerSesion, suscribirseASesion, type Sesion } from '@/services/sesion'

/** Sesión actual; el componente se vuelve a pintar cuando alguien inicia o cierra sesión. */
export function useSesion(): Sesion | null {
  return useSyncExternalStore(suscribirseASesion, obtenerSesion, obtenerSesion)
}
