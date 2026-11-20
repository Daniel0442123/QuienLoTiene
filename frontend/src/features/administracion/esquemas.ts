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
