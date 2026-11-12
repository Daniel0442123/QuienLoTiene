import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it } from 'vitest'
import { CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { haySegundoFactorPendiente, iniciarSesion, limpiarSesion } from '@/services/sesion'
import { InicioSesionPagina } from './InicioSesionPagina'
import { SegundoFactorPagina } from './SegundoFactorPagina'

function rutas() {
  return (
    <Routes>
      <Route path="/entrar" element={<InicioSesionPagina />} />
      <Route path="/entrar/codigo" element={<SegundoFactorPagina />} />
    </Routes>
  )
}

describe('HU-004 · Paso del inicio de sesión al código', () => {
  beforeEach(() => limpiarSesion())

  it('el administrador pasa a la pantalla del código después de escribir su frase', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(rutas(), { ruta: '/entrar' })

    // Cuando
    await usuario.type(screen.getByRole('textbox', { name: 'Correo' }), CORREOS_DE_PRUEBA.administrador)
    await usuario.type(screen.getByLabelText('Frase de contraseña'), FRASE_DE_PRUEBA)
    await usuario.click(screen.getByRole('button', { name: 'Entrar' }))

    // Entonces
    expect(await screen.findByRole('heading', { level: 1, name: 'Código de verificación' })).toBeInTheDocument()
  })

  it('sin un inicio de sesión en curso (por ejemplo al recargar) vuelve a entrar', async () => {
    // Dado / Cuando
    renderizarConProveedores(rutas(), { ruta: '/entrar/codigo' })

    // Entonces
    expect(await screen.findByRole('heading', { level: 1, name: 'Entrar' })).toBeInTheDocument()
  })

  it('«Entrar con otra cuenta» descarta el token temporal', async () => {
    // Dado
    const usuario = userEvent.setup()
    await iniciarSesion(CORREOS_DE_PRUEBA.administrador, FRASE_DE_PRUEBA)
    renderizarConProveedores(rutas(), { ruta: '/entrar/codigo' })

    // Cuando
    await usuario.click(screen.getByRole('link', { name: 'Entrar con otra cuenta' }))

    // Entonces
    expect(await screen.findByRole('heading', { level: 1, name: 'Entrar' })).toBeInTheDocument()
    expect(haySegundoFactorPendiente()).toBe(false)
  })
})
