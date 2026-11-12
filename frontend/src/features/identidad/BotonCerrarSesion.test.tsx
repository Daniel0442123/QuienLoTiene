import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA } from '@/mocks/datos'
import { tokenPara } from '@/mocks/utilidades'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { guardarSesion, obtenerSesion } from '@/services/sesion'
import { BotonCerrarSesion } from './BotonCerrarSesion'

function iniciarSesionDePrueba() {
  const cliente = base.usuarios.find((u) => u.correo === CORREOS_DE_PRUEBA.cliente)
  if (!cliente) throw new Error('Falta el cliente de prueba en la simulación')
  // El token se arma con la simulación: un valor escrito aquí parece una llave real para Gitleaks
  guardarSesion({
    usuarioId: cliente.id,
    rol: 'CLIENTE',
    tokenAcceso: tokenPara(cliente),
    tipoToken: 'Bearer',
    fechaExpiracion: '2026-11-11T15:30:00Z',
    segundoFactorRequerido: false,
  })
}

describe('HU-003 · Cerrar sesión', () => {
  it('no tiene violaciones de accesibilidad detectables', async () => {
    // Dado
    const { container } = renderizarConProveedores(<BotonCerrarSesion />)

    // Entonces
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('borra la sesión de memoria, lo anuncia y lleva a entrar', async () => {
    // Dado
    const usuario = userEvent.setup()
    iniciarSesionDePrueba()
    renderizarConProveedores(
      <Routes>
        <Route path="/" element={<BotonCerrarSesion />} />
        <Route path="/entrar" element={<h1>Entrar</h1>} />
      </Routes>,
    )

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'Cerrar sesión' }))

    // Entonces
    expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument()
    expect(obtenerSesion()).toBeNull()
    await vi.waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Sesión cerrada.'))
  })
})
