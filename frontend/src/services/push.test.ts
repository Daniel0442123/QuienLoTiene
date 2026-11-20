import { describe, expect, it } from 'vitest'
import { CLAVE_PUBLICA_SIMULADA } from '@/mocks/manejadores-notificaciones'
import { claveAplicacion, pushSoportado } from './push'

describe('HU-031 · Clave VAPID y soporte del navegador', () => {
  it('la clave pública en base64url se convierte en los 65 bytes que pide PushManager.subscribe', () => {
    const bytes = claveAplicacion(CLAVE_PUBLICA_SIMULADA)
    expect(bytes).toHaveLength(65)
    // Una clave P-256 sin comprimir empieza por 0x04
    expect(bytes[0]).toBe(4)
  })

  it('acepta la clave con o sin relleno y con los caracteres de base64url', () => {
    expect(Array.from(claveAplicacion('-_8'))).toEqual([0xfb, 0xff])
    expect(Array.from(claveAplicacion('-_8='))).toEqual([0xfb, 0xff])
  })

  it('jsdom no tiene Push API: sin soporte', () => {
    expect(pushSoportado()).toBe(false)
  })
})
