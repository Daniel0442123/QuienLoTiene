import { formatearPesos, formatearUnidades } from '@/services/formato'
import type { RespuestaDeTienda } from './api'
import { textosSolicitudes as textos } from './textos'

/** id del `article` de cada solicitud nueva: al deshacer «No lo tengo» el foco vuelve ahí. */
export const idTarjetaSolicitud = (solicitudId: string) => `solicitud-${solicitudId}`

/** Resumen de lo que respondió la tienda: «Sí lo tengo · $ 8.000 · 2 unidades». */
export function resumenDeRespuesta(respuesta: RespuestaDeTienda): string {
  return textos.resumenRespuesta(
    textos.tiposRespuesta[respuesta.tipo],
    formatearPesos(respuesta.precioEnPesos ?? 0),
    formatearUnidades(respuesta.cantidadDisponible ?? 0),
  )
}
