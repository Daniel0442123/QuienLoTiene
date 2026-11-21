import { formatearHoraDelDia } from '@/services/formato'
import type { DiaSemana, Franja } from './api'
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
