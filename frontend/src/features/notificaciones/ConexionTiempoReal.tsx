import { useTiempoReal } from './useTiempoReal'

/** Conexión de tiempo real de toda la app (HU-030). No pinta nada: los avisos salen por la región aria-live. */
export function ConexionTiempoReal() {
  useTiempoReal()
  return null
}
