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
