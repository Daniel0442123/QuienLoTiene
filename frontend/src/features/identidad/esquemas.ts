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

export const LARGO_MAXIMO_NOMBRE = 120

const nombre = z.string().trim().min(1, textos.nombreObligatorio).max(LARGO_MAXIMO_NOMBRE, textos.nombreMuyLargo)

/** Mismo patrón del backend (^\+?[0-9]{7,15}$). Antes se quitan espacios, guiones y paréntesis que la gente escribe. */
const telefono = z
  .string()
  .transform((valor) => valor.replace(/[\s\-()]/g, ''))
  .pipe(z.string().regex(/^\+?[0-9]{7,15}$/, textos.telefonoInvalido))

export const esquemaFraseNueva = z
  .string()
  .max(LARGO_MAXIMO_FRASE, textos.fraseMuyLarga)
  .superRefine((frase, contexto) => {
    if (frase.length === 0) {
      contexto.addIssue({ code: 'custom', message: textos.fraseObligatoria })
    } else if (frase.length < LARGO_MINIMO_FRASE) {
      contexto.addIssue({ code: 'custom', message: mensajeFraseCorta(frase.length) })
    }
  })

/** Registro de cliente (HU-001, HU-005). Igual a RegistrarClienteRequest, sin la versión de la política (la pone api.ts). */
export const esquemaRegistroCliente = z.object({
  nombre,
  correo,
  telefono,
  fraseContrasena: esquemaFraseNueva,
  aceptaPoliticaDatos: z.boolean().refine((acepta) => acepta, textos.politicaObligatoria),
})

/** Lo que escribe la persona (antes de limpiar el teléfono). */
export type EntradaRegistroCliente = z.input<typeof esquemaRegistroCliente>
/** Lo que se envía al backend. */
export type DatosRegistroCliente = z.output<typeof esquemaRegistroCliente>
