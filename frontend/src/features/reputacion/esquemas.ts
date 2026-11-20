import { z } from 'zod'
import { textosReputacion as textos } from './textos'

/** Calificar (HU-034), con los límites de CalificarRequest: de 1 a 5 estrellas y comentario opcional de hasta 500. */

export const LARGO_MAXIMO_COMENTARIO = 500
export const OPCIONES_ESTRELLAS = ['1', '2', '3', '4', '5'] as const

export const esquemaCalificar = z.object({
  // Los radios entregan texto, y null si no se eligió ninguno (como en DEF-005): mismo mensaje en los dos casos
  estrellas: z.enum(OPCIONES_ESTRELLAS, { error: textos.estrellasObligatorias }).transform(Number),
  comentario: z
    .string()
    .trim()
    .max(LARGO_MAXIMO_COMENTARIO, textos.comentarioLargo(LARGO_MAXIMO_COMENTARIO))
    // Vacío no se envía (el backend no recibe null en campos opcionales, C-04)
    .transform((texto) => (texto === '' ? undefined : texto)),
})

export type EntradaCalificar = z.input<typeof esquemaCalificar>
export type DatosCalificar = z.output<typeof esquemaCalificar>

/** Reportar (HU-035), con los límites de ReportarRequest: motivo de la lista y descripción hasta 1000, obligatoria con OTRO. */
export const LARGO_MAXIMO_DESCRIPCION = 1000
export const MOTIVOS_REPORTE = ['DEMORA_ENTREGA', 'PRODUCTO_DIFERENTE', 'COBRO_DIFERENTE', 'OTRO'] as const

export const esquemaReportar = z
  .object({
    // Sin elegir llega null (radios): mismo mensaje en los dos casos
    motivo: z.enum(MOTIVOS_REPORTE, { error: textos.motivoObligatorio }),
    descripcion: z.string().trim().max(LARGO_MAXIMO_DESCRIPCION, textos.descripcionLarga(LARGO_MAXIMO_DESCRIPCION)),
  })
  .superRefine(({ motivo, descripcion }, contexto) => {
    if (motivo === 'OTRO' && descripcion === '') {
      contexto.addIssue({ code: 'custom', path: ['descripcion'], message: textos.descripcionObligatoria })
    }
  })
  // Vacía no se envía (el backend no recibe null en campos opcionales, C-04)
  .transform(({ motivo, descripcion }) => ({ motivo, ...(descripcion ? { descripcion } : {}) }))

export type EntradaReportar = z.input<typeof esquemaReportar>
export type DatosReportar = z.output<typeof esquemaReportar>
