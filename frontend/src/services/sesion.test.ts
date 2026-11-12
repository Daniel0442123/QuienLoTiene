import { http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it } from 'vitest'
import { CODIGO_TOTP_DE_PRUEBA, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import {
  guardarSesion,
  haySegundoFactorPendiente,
  iniciarSesion,
  limpiarSesion,
  obtenerSesion,
  renovarSesion,
  verificarSegundoFactor,
} from './sesion'

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

describe('HU-004 · Segundo factor del administrador', () => {
  beforeEach(() => limpiarSesion())

  it('CP-IDE-004-01 · guarda el token temporal en memoria y con el código correcto entrega la sesión ADMIN', async () => {
    // Dado
    const respuesta = await iniciarSesion(CORREOS_DE_PRUEBA.administrador, FRASE_DE_PRUEBA)
    expect(respuesta.segundoFactorRequerido).toBe(true)
    expect(obtenerSesion()).toBeNull()
    expect(haySegundoFactorPendiente()).toBe(true)

    // Cuando
    const sesion = await verificarSegundoFactor(CODIGO_TOTP_DE_PRUEBA)

    // Entonces
    expect(sesion.rol).toBe('ADMIN')
    expect(obtenerSesion()?.rol).toBe('ADMIN')
    expect(haySegundoFactorPendiente()).toBe(false)
    expect(window.localStorage.length + window.sessionStorage.length).toBe(0)
  })

  it('CP-IDE-004-02 · con un código incorrecto no entrega sesión y deja intentar de nuevo', async () => {
    // Dado
    await iniciarSesion(CORREOS_DE_PRUEBA.administrador, FRASE_DE_PRUEBA)

    // Cuando
    const intento = verificarSegundoFactor('000000')

    // Entonces
    await expect(intento).rejects.toMatchObject({ codigo: 'IDE-012' })
    expect(obtenerSesion()).toBeNull()
    expect(haySegundoFactorPendiente()).toBe(true)
  })

  it('IDE-013 · al agotar los 5 intentos descarta el token temporal', async () => {
    // Dado
    await iniciarSesion(CORREOS_DE_PRUEBA.administrador, FRASE_DE_PRUEBA)
    for (let i = 0; i < 5; i++) await verificarSegundoFactor('000000').catch(() => undefined)

    // Cuando
    const intento = verificarSegundoFactor(CODIGO_TOTP_DE_PRUEBA)

    // Entonces
    await expect(intento).rejects.toMatchObject({ codigo: 'IDE-013' })
    expect(haySegundoFactorPendiente()).toBe(false)
  })

  it('IDE-013 · sin token temporal no llama al backend', async () => {
    await expect(verificarSegundoFactor(CODIGO_TOTP_DE_PRUEBA)).rejects.toMatchObject({ codigo: 'IDE-013' })
  })

  it('el token temporal deja de contar como pendiente cuando vence', async () => {
    // Dado
    await iniciarSesion(CORREOS_DE_PRUEBA.administrador, FRASE_DE_PRUEBA)

    // Entonces
    expect(haySegundoFactorPendiente(new Date(Date.now() + 6 * 60 * 1000))).toBe(false)
  })
})
