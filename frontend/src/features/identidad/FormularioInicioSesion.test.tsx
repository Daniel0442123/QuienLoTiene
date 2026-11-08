import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { CORREO_REGISTRADO, FRASE_CORRECTA } from '@/mocks/manejadores'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { obtenerSesion } from '@/services/sesion'
import { FormularioInicioSesion } from './FormularioInicioSesion'

describe('HU-003 · Iniciar sesión', () => {
  it('no tiene violaciones de accesibilidad detectables', async () => {
    // Dado
    const { container } = renderizarConProveedores(<FormularioInicioSesion alIniciar={vi.fn()} />)

    // Entonces
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('muestra el error del correo al salir del campo y lo asocia al campo', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioInicioSesion alIniciar={vi.fn()} />)
    const campoCorreo = screen.getByRole('textbox', { name: 'Correo' })

    // Cuando
    await usuario.type(campoCorreo, 'correo-sin-arroba')
    await usuario.tab()

    // Entonces
    expect(campoCorreo).toHaveAttribute('aria-invalid', 'true')
    expect(campoCorreo).toHaveAccessibleDescription(/correo válido/)
  })

  it('CP-IDE-003-01 · inicia sesión y guarda el token solo en memoria', async () => {
    // Dado
    const usuario = userEvent.setup()
    const alIniciar = vi.fn()
    renderizarConProveedores(<FormularioInicioSesion alIniciar={alIniciar} />)

    // Cuando
    await usuario.type(screen.getByRole('textbox', { name: 'Correo' }), CORREO_REGISTRADO)
    await usuario.type(screen.getByLabelText('Frase de contraseña'), FRASE_CORRECTA)
    await usuario.click(screen.getByRole('button', { name: 'Entrar' }))

    // Entonces
    await vi.waitFor(() => expect(alIniciar).toHaveBeenCalled())
    expect(obtenerSesion()?.rol).toBe('CLIENTE')
    expect(window.localStorage.length).toBe(0)
    expect(window.sessionStorage.length).toBe(0)
  })

  it('CP-IDE-003-02 · con credenciales incorrectas muestra un mensaje que no dice cuál falló', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioInicioSesion alIniciar={vi.fn()} />)

    // Cuando
    await usuario.type(screen.getByRole('textbox', { name: 'Correo' }), CORREO_REGISTRADO)
    await usuario.type(screen.getByLabelText('Frase de contraseña'), 'otra frase cualquiera')
    await usuario.click(screen.getByRole('button', { name: 'Entrar' }))

    // Entonces
    const mensaje = await screen.findByText(/El correo o la frase de contraseña no coinciden/)
    expect(mensaje).toHaveAttribute('role', 'alert')
  })

  it('permite ver la frase escrita (WCAG 3.3.8)', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioInicioSesion alIniciar={vi.fn()} />)

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'Mostrar frase' }))

    // Entonces
    expect(screen.getByLabelText('Frase de contraseña')).toHaveAttribute('type', 'text')
  })
})
