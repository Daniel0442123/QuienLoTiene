import { describe, expect, it } from 'vitest'
import { numeroCorto, pasosDelPedido, siguientePasoDeLaTienda } from './presentacion'

describe('HU-027 · Pasos del pedido · CP-ORD-027-01, CP-ORD-027-02', () => {
  it('cambia el tercer paso según la entrega', () => {
    expect(pasosDelPedido('DOMICILIO')).toEqual(['CONFIRMADO', 'PREPARANDO', 'EN_CAMINO', 'ENTREGADO', 'RESUELTA'])
    expect(pasosDelPedido('RECOGER')[2]).toBe('LISTO_PARA_RECOGER')
  })

  it('CP-ORD-027-01 · ofrece a la tienda solo el paso siguiente válido', () => {
    expect(siguientePasoDeLaTienda('CONFIRMADO', 'DOMICILIO')).toBe('PREPARANDO')
    expect(siguientePasoDeLaTienda('PREPARANDO', 'DOMICILIO')).toBe('EN_CAMINO')
    expect(siguientePasoDeLaTienda('PREPARANDO', 'RECOGER')).toBe('LISTO_PARA_RECOGER')
    expect(siguientePasoDeLaTienda('EN_CAMINO', 'DOMICILIO')).toBe('ENTREGADO')
    expect(siguientePasoDeLaTienda('LISTO_PARA_RECOGER', 'RECOGER')).toBe('ENTREGADO')
  })

  it('CP-ORD-027-02 · no ofrece nada cuando ya no le toca a la tienda', () => {
    for (const estado of ['ENTREGADO', 'RESUELTA', 'CANCELADO', 'VENCIDA'] as const) {
      expect(siguientePasoDeLaTienda(estado, 'DOMICILIO')).toBeNull()
    }
  })

  it('muestra el número corto como el backend', () => {
    expect(numeroCorto('e1f2a3b4-0001-4c5d-8e9f-0a1b2c3d4e01')).toBe('E1F2A3B4')
  })
})
