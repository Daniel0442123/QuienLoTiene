import { z } from 'zod'
import { textosAdministracion as textos } from './textos'

/** Motivo del rechazo (HU-036, RechazarTiendaRequest): de 5 a 300 caracteres (TIE-005). */
export const LARGO_MAXIMO_MOTIVO = 300

export const esquemaRechazo = z.object({
  motivo: z
    .string()
    .trim()
    .superRefine((motivo, contexto) => {
      if (motivo.length < 5) contexto.addIssue({ code: 'custom', message: textos.motivoCorto(motivo.length) })
      else if (motivo.length > LARGO_MAXIMO_MOTIVO) {
        contexto.addIssue({ code: 'custom', message: textos.motivoLargo(motivo.length) })
      }
    }),
})

export type DatosRechazo = z.infer<typeof esquemaRechazo>

/** HU-038: palabra clave restringida, de 2 a 80 caracteres (AgregarRestringidoRequest, CAT-003). */
export const esquemaPalabraRestringida = z.object({
  palabraClave: z.string().trim().min(2, textos.palabraCorta).max(80, textos.palabraLarga),
})

export type DatosPalabraRestringida = z.infer<typeof esquemaPalabraRestringida>

/** HU-037: nota de resolución, de 3 a 500 caracteres (ResolverReporteRequest). */
export const LARGO_MAXIMO_NOTA = 500

export const esquemaResolverReporte = z.object({
  nota: z.string().trim().min(3, textos.notaCorta).max(LARGO_MAXIMO_NOTA, textos.notaLarga),
})

export type DatosResolverReporte = z.infer<typeof esquemaResolverReporte>

/** HU-037: motivo de la suspensión, de 3 a 300 caracteres (SuspenderTiendaRequest). */
export const LARGO_MAXIMO_MOTIVO_SUSPENSION = 300

export const esquemaSuspender = z.object({
  motivo: z
    .string()
    .trim()
    .min(3, textos.motivoSuspensionCorto)
    .max(LARGO_MAXIMO_MOTIVO_SUSPENSION, textos.motivoSuspensionLargo),
})

export type DatosSuspender = z.infer<typeof esquemaSuspender>
