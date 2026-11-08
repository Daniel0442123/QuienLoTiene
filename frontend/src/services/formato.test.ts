import { describe, expect, it } from 'vitest'
import { formatearDistancia, formatearFechaHora, formatearPesos, minutosHasta } from './formato'

describe('Formato para Colombia', () => {
  it('muestra pesos sin decimales y con punto de miles', () => {
    expect(formatearPesos(4000).replace(/\s/g, ' ')).toBe('$ 4.000')
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
})
