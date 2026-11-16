import { describe, expect, it } from 'vitest'
import { crearEsquemaResponder } from './esquemas'

const esquema = crearEsquemaResponder(2)
const valida = { tipo: 'SI', precio: '8000', cantidad: '2', descripcionAlternativa: '' } as const

const mensajes = (entrada: object) =>
  esquema.safeParse({ ...valida, ...entrada }).error?.issues.map(({ path, message }) => [path.join('.'), message])

describe('HU-019 · Esquema de la respuesta de la tienda', () => {
  it('CP-SOL-019-01 · acepta «Sí lo tengo» a $ 8.000 por 2 unidades y entiende el punto de miles', () => {
    expect(esquema.parse(valida)).toMatchObject({ tipo: 'SI', precio: 8000, cantidad: 2 })
    expect(esquema.parse({ ...valida, precio: '$ 8.000' }).precio).toBe(8000)
  })

  it('CP-SOL-019-02 · pide la descripción con «Tengo algo parecido» y no con «Sí lo tengo»', () => {
    expect(mensajes({ tipo: 'PARECIDO', descripcionAlternativa: '   ' })).toEqual([
      ['descripcionAlternativa', 'Describe qué producto ofreces.'],
    ])
    expect(mensajes({ tipo: 'PARECIDO', descripcionAlternativa: 'a'.repeat(301) })).toEqual([
      ['descripcionAlternativa', 'Escribe máximo 300 caracteres. Llevas 301.'],
    ])
    expect(esquema.safeParse({ ...valida, descripcionAlternativa: '' }).success).toBe(true)
  })

  it('pide el precio, lo limita a $ 100.000.000 y no acepta decimales', () => {
    expect(mensajes({ precio: '' })).toEqual([['precio', 'Escribe el precio por unidad.']])
    expect(mensajes({ precio: '0' })?.[0]?.[0]).toBe('precio')
    expect(mensajes({ precio: '100000001' })?.[0]?.[0]).toBe('precio')
    expect(mensajes({ precio: '8000,50' })?.[0]?.[0]).toBe('precio')
  })

  it('no deja ofrecer más unidades de las que pidió el cliente ni menos de 1', () => {
    const mensaje = 'Escribe cuántas unidades tienes, de 1 a 2.'
    expect(mensajes({ cantidad: '3' })).toEqual([['cantidad', mensaje]])
    expect(mensajes({ cantidad: '0' })).toEqual([['cantidad', mensaje]])
    expect(mensajes({ cantidad: 'dos' })).toEqual([['cantidad', mensaje]])
  })
})
