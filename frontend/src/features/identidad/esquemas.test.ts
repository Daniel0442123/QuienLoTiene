import { describe, expect, it } from 'vitest'
import { esquemaFraseNueva, esquemaInicioSesion, mensajeFraseCorta } from './esquemas'

describe('HU-001 · HU-003 · Esquemas de identidad', () => {
  it('acepta un correo con espacios alrededor y lo recorta', () => {
    const resultado = esquemaInicioSesion.safeParse({ correo: '  ana@correo.com ', fraseContrasena: 'x' })
    expect(resultado.success && resultado.data.correo).toBe('ana@correo.com')
  })

  it('rechaza un correo de más de 254 caracteres', () => {
    const correoLargo = `${'a'.repeat(250)}@correo.com`
    expect(esquemaInicioSesion.safeParse({ correo: correoLargo, fraseContrasena: 'x' }).success).toBe(false)
  })

  it('CP-IDE-001-02 · la frase nueva de 14 caracteres dice cuántos lleva', () => {
    const resultado = esquemaFraseNueva.safeParse('a'.repeat(14))
    expect(resultado.success).toBe(false)
    expect(resultado.error?.issues[0]?.message).toBe(mensajeFraseCorta(14))
  })

  it('acepta una frase nueva de exactamente 15 caracteres', () => {
    expect(esquemaFraseNueva.safeParse('a'.repeat(15)).success).toBe(true)
  })

  it('rechaza una frase de 129 caracteres', () => {
    expect(esquemaFraseNueva.safeParse('a'.repeat(129)).success).toBe(false)
  })
})
