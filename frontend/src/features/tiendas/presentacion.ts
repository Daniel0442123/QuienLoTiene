import { formatearHoraDelDia } from '@/services/formato'
import type { DiaSemana, Franja, MedioPago, TiendaPublica } from './api'
import { DIAS_SEMANA } from './esquemas'
import { textosTiendas as textos } from './textos'

/** HU-014: el horario de lunes a domingo, con las franjas de cada día o «Cerrado». */
export function horarioPorDia(horario: Franja[]): Array<{ dia: DiaSemana; texto: string }> {
  return DIAS_SEMANA.map((dia) => {
    const franjas = horario
      .filter((franja) => franja.diaSemana === dia)
      .sort((a, b) => a.abre.localeCompare(b.abre))
      .map((franja) => textos.franja(formatearHoraDelDia(franja.abre), formatearHoraDelDia(franja.cierra)))
    return { dia, texto: franjas.length > 0 ? franjas.join(' y ') : textos.cerradoEseDia }
  })
}

/**
 * Medios de pago del detalle público. /v3/api-docs publica `mediosPago` como un solo valor, pero el backend manda una
 * lista (TiendaPublica.java, List<String>): se aceptan las dos formas hasta que se corrija el contrato (aviso a Daniel).
 */
export function mediosDePago(tienda: TiendaPublica): MedioPago[] {
  const valor: unknown = tienda.mediosPago
  if (Array.isArray(valor)) return valor as MedioPago[]
  return typeof valor === 'string' ? [valor as MedioPago] : []
}
