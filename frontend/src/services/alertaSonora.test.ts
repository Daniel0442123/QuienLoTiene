import { afterEach, describe, expect, it, vi } from 'vitest'

/** Contexto de audio falso: registra los tonos que se programan. */
function contextoFalso() {
  const tonos: number[] = []
  class ContextoFalso {
    currentTime = 0
    destination = {}
    createOscillator() {
      const oscilador = {
        frequency: { value: 0 },
        connect: (destino: unknown) => destino,
        start: () => tonos.push(oscilador.frequency.value),
        stop: vi.fn(),
      }
      return oscilador
    }
    createGain() {
      return { gain: { setValueAtTime: vi.fn(), exponentialRampToValueAtTime: vi.fn() }, connect: (d: unknown) => d }
    }
  }
  return { ContextoFalso, tonos }
}

describe('CP-NOT-030-02 · Alerta sonora de la solicitud urgente', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    vi.resetModules()
  })

  it('suena dos tonos cortos', async () => {
    const { ContextoFalso, tonos } = contextoFalso()
    vi.stubGlobal('AudioContext', ContextoFalso)
    const { sonarAlertaUrgente } = await import('./alertaSonora')

    sonarAlertaUrgente()

    expect(tonos).toEqual([880, 1175])
  })

  it('sin Web Audio no falla: el aviso en texto basta', async () => {
    vi.stubGlobal('AudioContext', undefined)
    const { sonarAlertaUrgente } = await import('./alertaSonora')
    expect(() => sonarAlertaUrgente()).not.toThrow()
  })

  it('si el navegador no deja crear el audio, no falla', async () => {
    vi.stubGlobal(
      'AudioContext',
      class {
        constructor() {
          throw new Error('No permitido sin un gesto del usuario')
        }
      },
    )
    const { sonarAlertaUrgente } = await import('./alertaSonora')
    expect(() => sonarAlertaUrgente()).not.toThrow()
  })
})
