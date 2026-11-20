import { describe, expect, it } from 'vitest'
import {
  formatearDistancia,
  formatearFecha,
  formatearHoraDelDia,
  formatearFechaHora,
  formatearHace,
  formatearPesos,
  formatearTamano,
  formatearUnidades,
  minutosHasta,
} from './formato'

describe('Formato para Colombia', () => {
  it('muestra pesos sin decimales y con punto de miles', () => {
    expect(formatearPesos(4000).replace(/\s/g, ' ')).toBe('$ 4.000')
  })

  it('CP-SOL-018-02 · muestra 1.200 m como «1,2 km»', () => {
    expect(formatearDistancia(1200)).toBe('1,2 km')
  })

  it('dice hace cuánto llegó una solicitud y cuántas unidades pide', () => {
    const ahora = new Date('2026-11-16T15:30:00Z')
    expect(formatearHace('2026-11-16T15:29:40Z', ahora)).toBe('hace un momento')
    expect(formatearHace('2026-11-16T15:26:00Z', ahora)).toBe('hace 4 min')
    expect(formatearHace('2026-11-16T13:00:00Z', ahora)).toBe('hace 2 h')
    expect(formatearUnidades(1)).toBe('1 unidad')
    expect(formatearUnidades(3)).toBe('3 unidades')
  })

  it('muestra metros por debajo de 1 km y kilómetros con un decimal por encima', () => {
    expect(formatearDistancia(850)).toBe('850 m')
    expect(formatearDistancia(3200)).toBe('3,2 km')
  })

  it('convierte la fecha UTC de la API a la hora de Bogotá', () => {
    expect(formatearFechaHora('2026-11-08T15:30:00Z')).toMatch(/10:30/)
  })

  it('HU-021 · cuenta los minutos que faltan para vencer y nunca da negativo', () => {
    const ahora = new Date('2026-11-08T15:00:00Z')
    expect(minutosHasta('2026-11-08T15:30:00Z', ahora)).toBe(30)
    expect(minutosHasta('2026-11-08T14:00:00Z', ahora)).toBe(0)
  })

  it('muestra el tamaño de un archivo en KB o MB', () => {
    expect(formatearTamano(200)).toBe('1 KB')
    expect(formatearTamano(850 * 1024)).toBe('850 KB')
    expect(formatearTamano(1.25 * 1024 * 1024)).toMatch(/^1,[23] MB$/)
  })

  it('HU-014 · las horas del horario se leen como el reloj, sin cambiar de zona', () => {
    expect(formatearHoraDelDia('08:00')).toMatch(/^8:00\sa\.\s?m\.$/)
    expect(formatearHoraDelDia('18:30')).toMatch(/^6:30\sp\.\s?m\.$/)
  })

  it('HU-014 · la fecha de una reseña es la de Colombia', () => {
    // 3:00 a. m. del 17 en UTC son las 10:00 p. m. del 16 en Colombia
    expect(formatearFecha('2026-11-17T03:00:00Z')).toBe('16/11/2026')
  })
})
