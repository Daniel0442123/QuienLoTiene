import { act, renderHook } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { DURACION_DESHACER_MS, useDescartarConDeshacer } from './useDescartarConDeshacer'

describe('HU-018 · «No lo tengo» con deshacer', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })
  afterEach(() => {
    vi.useRealTimers()
  })

  it('envía la respuesta cuando pasa el tiempo para deshacer, ni antes ni dos veces', () => {
    // Dado
    const enviar = vi.fn()
    const { result } = renderHook(() => useDescartarConDeshacer(enviar))

    // Cuando
    act(() => result.current.descartar('a'))
    act(() => vi.advanceTimersByTime(DURACION_DESHACER_MS - 1))

    // Entonces
    expect(enviar).not.toHaveBeenCalled()
    expect(result.current.pendiente).toBe('a')
    act(() => vi.advanceTimersByTime(1))
    expect(enviar).toHaveBeenCalledExactlyOnceWith('a')
    expect(result.current.pendiente).toBeNull()
  })

  it('al deshacer no envía nada y devuelve lo descartado', () => {
    // Dado
    const enviar = vi.fn()
    const { result } = renderHook(() => useDescartarConDeshacer(enviar))
    act(() => result.current.descartar('a'))

    // Cuando
    let devuelto: string | null = null
    act(() => {
      devuelto = result.current.deshacer()
    })
    act(() => vi.advanceTimersByTime(DURACION_DESHACER_MS))

    // Entonces
    expect(devuelto).toBe('a')
    expect(enviar).not.toHaveBeenCalled()
  })

  it('envía la anterior al descartar otra, al cerrar el aviso y al salir de la pantalla', () => {
    // Dado
    const enviar = vi.fn()
    const { result, unmount } = renderHook(() => useDescartarConDeshacer(enviar))

    // Cuando
    act(() => result.current.descartar('a'))
    act(() => result.current.descartar('b'))
    act(() => result.current.confirmar())
    act(() => result.current.descartar('c'))
    unmount()

    // Entonces
    expect(enviar.mock.calls).toEqual([['a'], ['b'], ['c']])
    vi.advanceTimersByTime(DURACION_DESHACER_MS)
    expect(enviar).toHaveBeenCalledTimes(3)
  })
})
