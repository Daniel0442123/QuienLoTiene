import type { Coordenadas } from '@/components/MapaUbicacion'

/** Por qué no se pudo obtener la ubicación: la persona no dio el permiso, o el GPS no respondió. */
export type FallaUbicacion = 'negado' | 'fallo'

/**
 * La ubicación de este momento con el GPS del celular (HU-008: «Repetir» usa la ubicación actual, no la de la
 * solicitud original). Rechaza con 'negado' o 'fallo'. Mismas opciones que «Usar mi ubicación» (SelectorUbicacion).
 */
export function obtenerUbicacionActual(): Promise<Coordenadas> {
  return new Promise((resolver, rechazar) => {
    if (!('geolocation' in navigator)) {
      rechazar('fallo' satisfies FallaUbicacion)
      return
    }
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => resolver({ latitud: coords.latitude, longitud: coords.longitude }),
      (falla) => rechazar((falla.code === falla.PERMISSION_DENIED ? 'negado' : 'fallo') satisfies FallaUbicacion),
      { enableHighAccuracy: true, timeout: 15_000, maximumAge: 60_000 },
    )
  })
}
