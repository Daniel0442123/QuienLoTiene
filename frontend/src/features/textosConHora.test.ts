import { describe, expect, it } from 'vitest'
import { textosOrdenes } from '@/features/ordenes/textos'
import { textosSolicitudes } from '@/features/solicitudes/textos'
import { formatearHora } from '@/services/formato'

// La hora de Colombia en la mañana y en la noche: formatearHora devuelve «a. m.» o «p. m.», con su punto
const HORAS = ['2026-11-16T14:05:00Z', '2026-11-17T03:17:00Z'].map(formatearHora)

/** Todos los textos que llevan la hora, con cada una de las horas de prueba. */
function textosConHora(hora: string) {
  return {
    plazoTerminado: textosSolicitudes.plazoTerminado(hora),
    yaRespondiste: textosSolicitudes.yaRespondiste('Sí lo tengo', '$ 8.000', '1 unidad', hora),
    vencio: textosSolicitudes.vencio(hora),
    vence: textosSolicitudes.vence(hora, 12),
    entregadoPregunta: textosOrdenes.entregadoPregunta('Droguería La 5', hora),
    // Sprint 3: HU-029 y HU-025
    canceloCliente: textosOrdenes.canceladoPor.CLIENTE.cliente(hora),
    canceloClienteVistoPorTienda: textosOrdenes.canceladoPor.CLIENTE.tienda(hora),
    canceloTienda: textosOrdenes.canceladoPor.TIENDA.cliente(hora, 'Droguería La 5'),
    canceloTiendaVistoPorTienda: textosOrdenes.canceladoPor.TIENDA.tienda(hora),
    reservaHasta: textosOrdenes.reservaHasta(hora),
    reservaHastaTienda: textosOrdenes.reservaHastaTienda(hora),
    reservaVencidaCliente: textosOrdenes.reservaVencidaCliente(hora),
    reservaVencidaTienda: textosOrdenes.reservaVencidaTienda(hora),
  }
}

describe('DEF-010 · textos que llevan la hora', () => {
  it('las horas de prueba terminan en «a. m.» y «p. m.» (el punto que causaba el doble punto)', () => {
    expect(HORAS[0]).toMatch(/a\.\s?m\.$/)
    expect(HORAS[1]).toMatch(/p\.\s?m\.$/)
  })

  it.each(HORAS)('con %s ningún texto tiene punto doble', (hora) => {
    // Se compara el objeto entero para que el fallo diga qué texto tiene el punto doble
    const conPuntoDoble = Object.entries(textosConHora(hora)).filter(([, texto]) => texto.includes('..'))
    expect(conPuntoDoble).toEqual([])
  })

  it('el ejemplo de la ficha ya no sale con «p. m..»', () => {
    expect(textosSolicitudes.plazoTerminado(HORAS[1]!)).toMatch(
      /^A las 10:17\sp\.\s?m\. terminó el plazo para responder\.$/,
    )
  })

  it('sin la hora de entrega, la pregunta no deja «a las» vacío', () => {
    expect(textosOrdenes.entregadoPregunta('Droguería La 5', '')).toBe(
      'Droguería La 5 marcó tu pedido como entregado. ¿Ya lo tienes?',
    )
  })
})
