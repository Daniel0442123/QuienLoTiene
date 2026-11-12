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

/** Días en el orden en que se muestran (lunes primero, como en Colombia). */
export const DIAS_SEMANA = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'] as const

const HORA = /^([01]\d|2[0-3]):[0-5]\d$/

/**
 * Perfil y horario (HU-009, ActualizarPerfilTiendaRequest). El horario se edita día por día (abre sí o no, y a qué
 * horas) y se envía como la lista de franjas que espera el backend: los días cerrados no se envían.
 */
export const esquemaPerfilTienda = z
  .object({
    nombre: textoObligatorio(textos.nombreTiendaObligatorio, textos.nombreTiendaMuyLargo),
    telefono: z
      .string()
      .transform((valor) => valor.replace(/[\s\-()]/g, ''))
      .pipe(z.string().regex(/^\+?[0-9]{7,15}$/, textos.telefonoInvalido)),
    dias: z
      .array(z.object({ diaSemana: z.enum(DIAS_SEMANA), abre: z.boolean(), apertura: z.string(), cierre: z.string() }))
      .length(DIAS_SEMANA.length),
  })
  .superRefine(({ dias }, contexto) => {
    dias.forEach((dia, indice) => {
      if (!dia.abre) return
      if (!HORA.test(dia.apertura)) {
        contexto.addIssue({ code: 'custom', path: ['dias', indice, 'apertura'], message: textos.horaObligatoria })
      }
      if (!HORA.test(dia.cierre)) {
        contexto.addIssue({ code: 'custom', path: ['dias', indice, 'cierre'], message: textos.horaObligatoria })
      } else if (HORA.test(dia.apertura) && dia.cierre <= dia.apertura) {
        contexto.addIssue({ code: 'custom', path: ['dias', indice, 'cierre'], message: textos.abreAntesDeCerrar })
      }
    })
  })
  .transform(({ nombre, telefono, dias }) => ({
    nombre,
    telefono,
    horario: dias
      .filter((dia) => dia.abre)
      .map(({ diaSemana, apertura, cierre }) => ({ diaSemana, abre: apertura, cierra: cierre })),
  }))

export type EntradaPerfilTienda = z.input<typeof esquemaPerfilTienda>
export type DatosPerfilTienda = z.output<typeof esquemaPerfilTienda>

const HORARIO_SUGERIDO = { apertura: '08:00', cierre: '18:00' }

/** Valores iniciales del formulario a partir de la tienda: los días sin franja quedan cerrados. */
export function perfilInicial(tienda: {
  nombre: string
  telefono: string
  horario: Array<{ diaSemana: string; abre: string; cierra: string }>
}): EntradaPerfilTienda {
  return {
    nombre: tienda.nombre,
    telefono: tienda.telefono,
    dias: DIAS_SEMANA.map((diaSemana) => {
      const franja = tienda.horario.find((f) => f.diaSemana === diaSemana)
      return franja
        ? { diaSemana, abre: true, apertura: franja.abre.slice(0, 5), cierre: franja.cierra.slice(0, 5) }
        : { diaSemana, abre: false, ...HORARIO_SUGERIDO }
    }),
  }
}

/** Radios de cobertura que se ofrecen (el backend acepta de 500 a 10.000 m). */
export const RADIOS_COBERTURA_METROS = [500, 1000, 2000, 3000, 5000, 8000, 10_000] as const

/** "$ 4.000", "4.000" o "4 000" → "4000": la gente escribe los pesos como los ve. */
const soloDigitos = (valor: string) => valor.replace(/[\s.$]/g, '')

/**
 * Domicilio con tarifa fija (HU-010, ConfigurarDomicilioRequest). Si no hace domicilio se envían radio y tarifa en null,
 * como espera el backend; si lo hace, los dos son obligatorios (TIE-009).
 */
export const esquemaDomicilio = z
  .object({ haceDomicilio: z.boolean(), radio: z.string(), tarifa: z.string() })
  .superRefine(({ haceDomicilio, radio, tarifa }, contexto) => {
    if (!haceDomicilio) return
    if (radio === '') contexto.addIssue({ code: 'custom', path: ['radio'], message: textos.radioObligatorio })
    const pesos = soloDigitos(tarifa)
    if (pesos === '') contexto.addIssue({ code: 'custom', path: ['tarifa'], message: textos.tarifaObligatoria })
    else if (!/^\d{1,9}$/.test(pesos)) {
      contexto.addIssue({ code: 'custom', path: ['tarifa'], message: textos.tarifaInvalida })
    }
  })
  .transform(({ haceDomicilio, radio, tarifa }) =>
    haceDomicilio
      ? { haceDomicilio, radioCoberturaEnMetros: Number(radio), tarifaFijaEnPesos: Number(soloDigitos(tarifa)) }
      : { haceDomicilio, radioCoberturaEnMetros: null, tarifaFijaEnPesos: null },
  )

export type EntradaDomicilio = z.input<typeof esquemaDomicilio>
export type DatosDomicilio = z.output<typeof esquemaDomicilio>

/** Pesos escritos por la persona → número, o null si todavía no es un valor válido. */
export function pesosEscritos(valor: string): number | null {
  const pesos = soloDigitos(valor)
  return /^\d{1,9}$/.test(pesos) ? Number(pesos) : null
}

/** Simulación del costo (CU-17 pasos 7 a 10). */
export const esquemaSimulacionDomicilio = z.object({
  subtotal: z
    .string()
    .refine((valor) => pesosEscritos(valor) !== null, textos.tarifaInvalida)
    .transform((valor) => pesosEscritos(valor) ?? 0),
  distancia: z.string().min(1, textos.radioObligatorio).transform(Number),
})

export type EntradaSimulacionDomicilio = z.input<typeof esquemaSimulacionDomicilio>
export type DatosSimulacionDomicilio = z.output<typeof esquemaSimulacionDomicilio>

export const MEDIOS_PAGO = ['EFECTIVO', 'TRANSFERENCIA', 'DATAFONO'] as const

/** Medios de pago (HU-012, ConfigurarMediosPagoRequest): al menos uno (TIE-011). */
export const esquemaMediosPago = z.object({
  mediosPago: z.array(z.enum(MEDIOS_PAGO)).min(1, textos.medioObligatorio),
})

export type DatosMediosPago = z.output<typeof esquemaMediosPago>
