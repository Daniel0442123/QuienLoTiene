import { z } from 'zod'
import { textosIdentidad as textos } from './textos'

/**
 * Esquemas de formulario con los mismos límites del backend (IniciarSesionRequest, CODIGOS-DE-ERROR IDE-004, IDE-005).
 * Validan la forma en el navegador; las reglas de negocio las decide el backend.
 */

export const LARGO_MAXIMO_CORREO = 254
export const LARGO_MINIMO_FRASE = 15
export const LARGO_MAXIMO_FRASE = 128

const correo = z
  .string()
  .trim()
  .min(1, textos.correoObligatorio)
  .max(LARGO_MAXIMO_CORREO, textos.correoMuyLargo)
  .pipe(z.email(textos.correoInvalido))

export const esquemaInicioSesion = z.object({
  correo,
  // En el inicio de sesión no se exige el mínimo de 15: el backend responde IDE-008 sin decir qué falló
  fraseContrasena: z.string().min(1, textos.fraseObligatoria).max(LARGO_MAXIMO_FRASE, textos.fraseMuyLarga),
})

export type DatosInicioSesion = z.infer<typeof esquemaInicioSesion>

/** Mensaje que dice cuánto falta, en lugar de solo "muy corta" (DECISIONES-FRONTEND sección 9). */
export function mensajeFraseCorta(largoActual: number): string {
  return `La frase debe tener al menos ${LARGO_MINIMO_FRASE} caracteres. Llevas ${largoActual}.`
}

export const esquemaFraseNueva = z
  .string()
  .max(LARGO_MAXIMO_FRASE, textos.fraseMuyLarga)
  .superRefine((frase, contexto) => {
    if (frase.length < LARGO_MINIMO_FRASE) {
      contexto.addIssue({ code: 'custom', message: mensajeFraseCorta(frase.length) })
    }
  })
