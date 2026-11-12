import { z } from 'zod'
import { problemaDeArchivo } from '@/services/archivos'
import { textosTiendas as textos } from './textos'

/**
 * Registro de tienda (HU-002), con los límites de RegistrarTiendaRequest del backend. Los documentos se revisan aquí
 * para que no falten y para que tengan el tipo y el tamaño que acepta el backend (services/archivos.ts).
 */

const LARGO_MAXIMO_NOMBRE = 120
const LARGO_MAXIMO_CORREO = 254
const LARGO_MINIMO_FRASE = 15
const LARGO_MAXIMO_FRASE = 128

const textoObligatorio = (obligatorio: string, muyLargo: string) =>
  z.string().trim().min(1, obligatorio).max(LARGO_MAXIMO_NOMBRE, muyLargo)

/** Documento obligatorio, con el tipo y el tamaño que acepta el backend (TIE-003, GEN-010). */
const documento = (mensajeObligatorio: string) =>
  z
    .instanceof(File, { message: mensajeObligatorio })
    .nullable()
    .superRefine((archivo, contexto) => {
      if (!archivo) return contexto.addIssue({ code: 'custom', message: mensajeObligatorio })
      const problema = problemaDeArchivo(archivo)
      if (problema === 'tipo') contexto.addIssue({ code: 'custom', message: textos.archivoTipo })
      else if (problema === 'tamano') contexto.addIssue({ code: 'custom', message: textos.archivoTamano })
    })

export const esquemaRegistroTienda = z.object({
  nombreDueno: textoObligatorio(textos.nombreObligatorio, textos.nombreMuyLargo),
  correo: z
    .string()
    .trim()
    .min(1, textos.correoObligatorio)
    .max(LARGO_MAXIMO_CORREO, textos.correoMuyLargo)
    .pipe(z.email(textos.correoInvalido)),
  telefono: z
    .string()
    .transform((valor) => valor.replace(/[\s\-()]/g, ''))
    .pipe(z.string().regex(/^\+?[0-9]{7,15}$/, textos.telefonoInvalido)),
  fraseContrasena: z
    .string()
    .max(LARGO_MAXIMO_FRASE, textos.fraseMuyLarga)
    .superRefine((frase, contexto) => {
      if (frase.length === 0) contexto.addIssue({ code: 'custom', message: textos.fraseObligatoria })
      else if (frase.length < LARGO_MINIMO_FRASE) {
        contexto.addIssue({ code: 'custom', message: textos.fraseCorta(frase.length) })
      }
    }),
  nombreTienda: textoObligatorio(textos.nombreTiendaObligatorio, textos.nombreTiendaMuyLargo),
  categoriaId: z.string({ message: textos.categoriaObligatoria }).min(1, textos.categoriaObligatoria),
  ubicacion: z
    .object({ latitud: z.number().min(-90).max(90), longitud: z.number().min(-180).max(180) })
    .nullable()
    .refine((ubicacion) => ubicacion !== null, textos.ubicacionObligatoria),
  fotoLocal: documento(textos.fotoObligatoria),
  rut: documento(textos.rutObligatorio),
  aceptaPoliticaDatos: z.boolean().refine((acepta) => acepta, textos.politicaObligatoria),
})

export type EntradaRegistroTienda = z.input<typeof esquemaRegistroTienda>
export type DatosRegistroTienda = z.output<typeof esquemaRegistroTienda>
