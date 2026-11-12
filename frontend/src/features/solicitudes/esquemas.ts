import { z } from 'zod'
import { textosSolicitudes as textos } from './textos'

/**
 * Publicar solicitud (HU-016), con los límites de PublicarSolicitudRequest. Para cumplir RNF-027 (≤ 60 s) solo se
 * escribe el producto: la modalidad arranca en «recoger», la cantidad en 1 y la ubicación en la primera dirección.
 */

const LARGO_MAXIMO_PRODUCTO = 150

export const MODALIDADES = ['RECOGER', 'DOMICILIO'] as const

export const esquemaPublicarSolicitud = z.object({
  producto: z
    .string()
    .trim()
    .min(1, textos.productoObligatorio)
    .superRefine((producto, contexto) => {
      if (producto.length > LARGO_MAXIMO_PRODUCTO) {
        contexto.addIssue({ code: 'custom', message: textos.productoMuyLargo(producto.length) })
      }
    }),
  categoriaId: z.string().min(1, textos.categoriaObligatoria),
  modalidad: z.enum(MODALIDADES),
  cantidad: z
    .string()
    .trim()
    .regex(/^\d{1,3}$/, textos.cantidadInvalida)
    .transform(Number)
    .refine((cantidad) => cantidad >= 1 && cantidad <= 999, textos.cantidadInvalida),
  esUrgente: z.boolean(),
  ubicacion: z
    .object({ latitud: z.number().min(-90).max(90), longitud: z.number().min(-180).max(180) })
    .nullable()
    .refine((ubicacion) => ubicacion !== null, textos.ubicacionObligatoria),
})

export type EntradaPublicarSolicitud = z.input<typeof esquemaPublicarSolicitud>
export type DatosPublicarSolicitud = z.output<typeof esquemaPublicarSolicitud>
