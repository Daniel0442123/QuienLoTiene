import { QueryClient } from '@tanstack/react-query'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { manejarEvento } from './useTiempoReal'

vi.mock('@/services/alertaSonora', () => ({ sonarAlertaUrgente: vi.fn() }))

function preparar() {
  const consultas = new QueryClient()
  const invalidar = vi.spyOn(consultas, 'invalidateQueries').mockResolvedValue()
  const anunciar = vi.fn()
  const claves = () => invalidar.mock.calls.map(([filtro]) => filtro?.queryKey)
  return { consultas, anunciar, claves }
}

describe('HU-030 · Qué hace cada evento del flujo', () => {
  afterEach(() => vi.clearAllMocks())

  it('orden-creada: refresca los pedidos y «Mis respuestas» y avisa a la tienda que la eligieron', () => {
    const { consultas, anunciar, claves } = preparar()
    manejarEvento('orden-creada', { ordenId: 'o', solicitudId: 's', producto: 'Curitas' }, consultas, anunciar)
    expect(claves()).toEqual([['ordenes'], ['tiendas', 'me', 'respuestas']])
    expect(anunciar).toHaveBeenCalledWith('Te eligieron: pedido nuevo de «Curitas».')
  })

  it('solicitud-cerrada y respuesta-no-elegida: refrescan las listas de la tienda sin anunciar', () => {
    const { consultas, anunciar, claves } = preparar()
    manejarEvento('solicitud-cerrada', { solicitudId: 's', estado: 'VENCIDA' }, consultas, anunciar)
    manejarEvento('respuesta-no-elegida', { solicitudId: 's', respuestaId: 'r' }, consultas, anunciar)
    expect(claves()).toEqual([
      ['solicitudes', 'disponibles'],
      ['tiendas', 'me', 'respuestas'],
      ['solicitudes', 'disponibles'],
      ['tiendas', 'me', 'respuestas'],
    ])
    expect(anunciar).not.toHaveBeenCalled()
  })

  it('solicitud-vencida y estado-orden-cambiado refrescan lo suyo; un tipo desconocido no hace nada', () => {
    const { consultas, anunciar, claves } = preparar()
    manejarEvento('solicitud-vencida', { solicitudId: 's', respuestasRecibidas: 0 }, consultas, anunciar)
    manejarEvento('estado-orden-cambiado', { ordenId: 'o', estado: 'PREPARANDO' }, consultas, anunciar)
    manejarEvento('evento-del-futuro', {}, consultas, anunciar)
    expect(claves()).toEqual([['solicitudes', 's'], ['ordenes']])
    expect(anunciar).not.toHaveBeenCalled()
  })

  it('HU-033 · mensaje-recibido: refresca solo el chat de esa orden, sin anunciar (lo lee su región role="log")', () => {
    const { consultas, anunciar, claves } = preparar()
    manejarEvento('mensaje-recibido', { ordenId: 'o-1', mensajeId: 'm-1' }, consultas, anunciar)
    expect(claves()).toEqual([['ordenes', 'o-1', 'mensajes']])
    expect(anunciar).not.toHaveBeenCalled()
  })
})
