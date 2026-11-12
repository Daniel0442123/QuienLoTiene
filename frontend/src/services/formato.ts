/** Formato de dinero, distancias y fechas para Colombia (DECISIONES-FRONTEND F-12). */

const ZONA_HORARIA = 'America/Bogota'
const IDIOMA = 'es-CO'

const formatoPesos = new Intl.NumberFormat(IDIOMA, {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
})

const formatoFechaHora = new Intl.DateTimeFormat(IDIOMA, {
  dateStyle: 'medium',
  timeStyle: 'short',
  timeZone: ZONA_HORARIA,
})

const formatoHora = new Intl.DateTimeFormat(IDIOMA, {
  hour: 'numeric',
  minute: '2-digit',
  timeZone: ZONA_HORARIA,
})

/** 4000 → "$ 4.000" (pesos enteros, como los envía la API). */
export function formatearPesos(valorEnPesos: number): string {
  return formatoPesos.format(valorEnPesos)
}

/** 850 → "850 m"; 3200 → "3,2 km". */
export function formatearDistancia(distanciaEnMetros: number): string {
  if (distanciaEnMetros < 1000) {
    return `${Math.round(distanciaEnMetros)} m`
  }
  const kilometros = new Intl.NumberFormat(IDIOMA, { maximumFractionDigits: 1 }).format(distanciaEnMetros / 1000)
  return `${kilometros} km`
}

/** Fecha ISO-8601 en UTC de la API → fecha y hora de Colombia. */
export function formatearFechaHora(fechaIso: string): string {
  return formatoFechaHora.format(new Date(fechaIso))
}

export function formatearHora(fechaIso: string): string {
  return formatoHora.format(new Date(fechaIso))
}

/** Minutos que faltan para una fecha (0 si ya pasó). Sirve para "vence en 12 min". */
export function minutosHasta(fechaIso: string, ahora: Date = new Date()): number {
  const diferenciaEnMilisegundos = new Date(fechaIso).getTime() - ahora.getTime()
  return Math.max(0, Math.ceil(diferenciaEnMilisegundos / 60_000))
}

const formatoMegabytes = new Intl.NumberFormat(IDIOMA, { maximumFractionDigits: 1 })

/** Tamaño de un archivo para mostrarlo a la persona: "850 KB", "1,2 MB". */
export function formatearTamano(bytes: number): string {
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`
  return `${formatoMegabytes.format(bytes / (1024 * 1024))} MB`
}
