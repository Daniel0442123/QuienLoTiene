import { http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it } from 'vitest'
import { servidorSimulado } from '@/mocks/servidor'
import { guardarSesion, limpiarSesion, obtenerSesion, renovarSesion } from './sesion'

const sesionRenovada = {
  usuarioId: 'u-1',
  rol: 'TIENDA',
  tokenAcceso: 'token-nuevo',
  tipoToken: 'Bearer',
  fechaExpiracion: '2026-11-08T15:45:00Z',
  segundoFactorRequerido: false,
} as const

describe('HU-003 · Renovación de la sesión', () => {
  beforeEach(() => limpiarSesion())

  it('recupera la sesión con la cookie de refresco', async () => {
    servidorSimulado.use(http.post('/api/v1/auth/refresco', () => HttpResponse.json(sesionRenovada)))
    const sesion = await renovarSesion()
    expect(sesion?.tokenAcceso).toBe('token-nuevo')
    expect(obtenerSesion()?.rol).toBe('TIENDA')
  })

  it('hace una sola renovación aunque la pidan varias peticiones a la vez', async () => {
    let renovaciones = 0
    servidorSimulado.use(
      http.post('/api/v1/auth/refresco', () => {
        renovaciones += 1
        return HttpResponse.json(sesionRenovada)
      }),
    )
    await Promise.all([renovarSesion(), renovarSesion(), renovarSesion()])
    expect(renovaciones).toBe(1)
  })

  it('IDE-010 · limpia la sesión si la cookie venció', async () => {
    guardarSesion({ ...sesionRenovada, tokenAcceso: 'token-viejo' })
    const sesion = await renovarSesion()
    expect(sesion).toBeNull()
    expect(obtenerSesion()).toBeNull()
  })
})
