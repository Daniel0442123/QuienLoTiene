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

/**
 * Responder una solicitud (HU-019), con los límites de ResponderSolicitudRequest. «No lo tengo» no pasa por aquí:
 * se envía con un toque desde la lista. La cantidad va de 1 a la que pidió el cliente (SOL-006).
 */

const PRECIO_MAXIMO = 100_000_000
const LARGO_MAXIMO_ALTERNATIVA = 300

export const TIPOS_RESPUESTA_CON_PRECIO = ['SI', 'PARECIDO'] as const

export function crearEsquemaResponder(cantidadPedida: number) {
  return z
    .object({
      tipo: z.enum(TIPOS_RESPUESTA_CON_PRECIO),
      // Se aceptan los puntos de miles que la gente escribe («8.000»); se envían solo los dígitos
      precio: z
        .string()
        .transform((valor) => valor.replace(/[\s.$]/g, ''))
        .pipe(
          // Vacío dice solo «Escribe el precio»; el formato se revisa después (pipe se detiene en el primer error)
          z
            .string()
            .min(1, textos.precioObligatorio)
            .pipe(z.string().regex(/^\d{1,9}$/, textos.precioInvalido))
            .transform(Number)
            .refine((precio) => precio >= 1 && precio <= PRECIO_MAXIMO, textos.precioInvalido),
        ),
      cantidad: z
        .string()
        .trim()
        .regex(/^\d{1,3}$/, textos.cantidadDisponibleInvalida(cantidadPedida))
        .transform(Number)
        .refine(
          (cantidad) => cantidad >= 1 && cantidad <= cantidadPedida,
          textos.cantidadDisponibleInvalida(cantidadPedida),
        ),
      descripcionAlternativa: z.string().trim(),
    })
    .superRefine(({ tipo, descripcionAlternativa }, contexto) => {
      if (tipo !== 'PARECIDO') return
      if (descripcionAlternativa === '') {
        contexto.addIssue({ code: 'custom', path: ['descripcionAlternativa'], message: textos.alternativaObligatoria })
      } else if (descripcionAlternativa.length > LARGO_MAXIMO_ALTERNATIVA) {
        contexto.addIssue({
          code: 'custom',
          path: ['descripcionAlternativa'],
          message: textos.alternativaMuyLarga(descripcionAlternativa.length),
        })
      }
    })
}

export type EntradaResponder = z.input<ReturnType<typeof crearEsquemaResponder>>
export type DatosResponder = z.output<ReturnType<typeof crearEsquemaResponder>>
