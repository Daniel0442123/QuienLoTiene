import { describe, expect, it } from 'vitest'
import { esquemaFraseNueva, esquemaInicioSesion, esquemaRegistroCliente, mensajeFraseCorta } from './esquemas'

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

  it('con la frase nueva vacía pide escribirla en vez de contar caracteres', () => {
    expect(esquemaFraseNueva.safeParse('').error?.issues[0]?.message).toBe('Escribe tu frase de contraseña.')
  })

  it('acepta una frase nueva de exactamente 15 caracteres', () => {
    expect(esquemaFraseNueva.safeParse('a'.repeat(15)).success).toBe(true)
  })

  it('rechaza una frase de 129 caracteres', () => {
    expect(esquemaFraseNueva.safeParse('a'.repeat(129)).success).toBe(false)
  })

  const registroValido = {
    nombre: ' Ana Pérez ',
    correo: 'ana@correo.com',
    telefono: '(300) 123-4567',
    fraseContrasena: 'mi perro come arepa',
    aceptaPoliticaDatos: true,
  }

  it('HU-001 · limpia espacios, guiones y paréntesis del celular y recorta el nombre', () => {
    const resultado = esquemaRegistroCliente.safeParse(registroValido)
    expect(resultado.success && resultado.data).toMatchObject({ nombre: 'Ana Pérez', telefono: '3001234567' })
  })

  it('HU-001 · rechaza un celular con letras o de menos de 7 dígitos', () => {
    expect(esquemaRegistroCliente.safeParse({ ...registroValido, telefono: '300abc' }).success).toBe(false)
    expect(esquemaRegistroCliente.safeParse({ ...registroValido, telefono: '123456' }).success).toBe(false)
  })

  it('CP-IDE-005-01 · exige marcar la autorización de datos', () => {
    const resultado = esquemaRegistroCliente.safeParse({ ...registroValido, aceptaPoliticaDatos: false })
    expect(resultado.error?.issues[0]?.path).toEqual(['aceptaPoliticaDatos'])
  })
})
