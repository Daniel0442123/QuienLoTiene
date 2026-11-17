import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { emitirEvento } from '@/mocks/eventos'
import { esperaParaReintento, escucharEventos } from './eventos'
import { iniciarSesion, limpiarSesion, obtenerSesion } from './sesion'

describe('HU-030 · Flujo de eventos en tiempo real', () => {
  let control: AbortController

  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA)
    control = new AbortController()
  })
  afterEach(() => control.abort())

  it('abre el flujo con el token en la cabecera y entrega cada evento con su tipo y sus datos', async () => {
    // Dado
    const recibidos: Array<[string, unknown]> = []
    void escucharEventos({ senal: control.signal, alEvento: (tipo, datos) => recibidos.push([tipo, datos]) })
    const usuarioId = obtenerSesion()!.usuarioId

    // Cuando: el servidor emite después de abrir la conexión
    await vi.waitFor(() => {
      emitirEvento([usuarioId], 'solicitud-nueva', { solicitudId: 'a', producto: 'Curitas', esUrgente: true })
      expect(recibidos.length).toBeGreaterThan(0)
    })

    // Entonces
    expect(recibidos[0]).toEqual(['solicitud-nueva', { solicitudId: 'a', producto: 'Curitas', esUrgente: true }])
  })

  it('al cerrar sesión deja de escuchar sin reintentar', async () => {
    // Dado
    limpiarSesion()
    const alEvento = vi.fn()

    // Cuando
    await escucharEventos({ senal: control.signal, alEvento })

    // Entonces: sin token ni siquiera abre la conexión
    expect(alEvento).not.toHaveBeenCalled()
  })

  it('reintenta con espera creciente: 1 s, 2 s, 5 s, 10 s y luego cada 30 s', () => {
    expect([0, 1, 2, 3, 4, 9].map(esperaParaReintento)).toEqual([1_000, 2_000, 5_000, 10_000, 30_000, 30_000])
  })
})
