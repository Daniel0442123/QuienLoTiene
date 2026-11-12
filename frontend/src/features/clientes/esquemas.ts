import { z } from 'zod'
import { textosClientes as textos } from './textos'

/** Perfil y direcciones del cliente (HU-007), con los límites de ActualizarPerfilRequest y DireccionRequest. */

const telefono = z
  .string()
  .transform((valor) => valor.replace(/[\s\-()]/g, ''))
  .pipe(z.string().regex(/^\+?[0-9]{7,15}$/, textos.telefonoInvalido))

export const esquemaPerfil = z.object({
  nombre: z.string().trim().min(1, textos.nombreObligatorio).max(120, textos.nombreMuyLargo),
  telefono,
})

export type EntradaPerfil = z.input<typeof esquemaPerfil>
export type DatosPerfil = z.output<typeof esquemaPerfil>

export const esquemaDireccion = z.object({
  nombre: z.string().trim().min(1, textos.nombreDireccionObligatorio).max(40, textos.nombreDireccionMuyLargo),
  ubicacion: z
    .object({ latitud: z.number().min(-90).max(90), longitud: z.number().min(-180).max(180) })
    .nullable()
    .refine((ubicacion) => ubicacion !== null, textos.ubicacionObligatoria),
  direccionEscrita: z.string().trim().min(1, textos.direccionObligatoria).max(200, textos.direccionMuyLarga),
  // Vacías se envían como null: el backend las trata como «sin indicaciones»
  indicaciones: z
    .string()
    .trim()
    .max(200, textos.indicacionesMuyLargas)
    .transform((valor) => (valor === '' ? null : valor)),
  telefono,
})

export type EntradaDireccion = z.input<typeof esquemaDireccion>
export type DatosDireccionFormulario = z.output<typeof esquemaDireccion>
