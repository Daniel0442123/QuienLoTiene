import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { Route, Routes } from 'react-router'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { guardarSesion, limpiarSesion, type Rol } from '@/services/sesion'
import { Plantilla } from './Plantilla'

// Lo que no es de la navegación queda fuera: tiempo real, aviso de versión nueva de la PWA y foco al cambiar de ruta
vi.mock('@/features/notificaciones/ConexionTiempoReal', () => ({ ConexionTiempoReal: () => null }))
vi.mock('./AvisoActualizacion', () => ({ AvisoActualizacion: () => null }))

function entrarComo(rol: Rol) {
  guardarSesion({
    usuarioId: `usuario-${rol}`,
    rol,
    tokenAcceso: 'token',
    fechaExpiracion: new Date(Date.now() + 15 * 60_000).toISOString(),
    tipoToken: 'Bearer',
    segundoFactorRequerido: false,
  })
}

function renderizarEn(ruta: string) {
  return renderizarConProveedores(
    <Routes>
      <Route element={<Plantilla />}>
        <Route path="*" element={<h1>Pantalla</h1>} />
      </Route>
    </Routes>,
    { ruta },
  )
}

const menu = () => screen.getByRole('navigation', { name: 'Menú principal' })
const enlacesDelMenu = () =>
  within(menu())
    .getAllByRole('link')
    .map((enlace) => enlace.textContent)

describe('SC-020 · Menú principal con íconos según el rol', () => {
  beforeEach(() => limpiarSesion())

  it('sin sesión no muestra menú ni botón de menú', () => {
    renderizarEn('/')
    expect(screen.queryByRole('navigation', { name: 'Menú principal' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Menú' })).not.toBeInTheDocument()
  })

  it('el cliente ve sus módulos y el actual va marcado con aria-current', async () => {
    entrarComo('CLIENTE')
    const { container } = renderizarEn('/solicitudes/abc')
    expect(enlacesDelMenu()).toEqual(['Inicio', 'Preguntar', 'Mis preguntas', 'Mis pedidos', 'Mi cuenta'])
    expect(within(menu()).getByRole('link', { name: 'Mis preguntas' })).toHaveAttribute('aria-current', 'page')
    expect(within(menu()).getByRole('link', { name: 'Preguntar' })).not.toHaveAttribute('aria-current')
    expect(within(menu()).getByRole('button', { name: 'Cerrar sesión' })).toBeInTheDocument()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('en «Preguntar» solo se marca ese módulo, no «Mis preguntas»', () => {
    entrarComo('CLIENTE')
    renderizarEn('/solicitudes/nueva')
    expect(within(menu()).getByRole('link', { name: 'Preguntar' })).toHaveAttribute('aria-current', 'page')
    expect(within(menu()).getByRole('link', { name: 'Mis preguntas' })).not.toHaveAttribute('aria-current')
  })

  it('la tienda ve su panel y sus secciones, y no los módulos del cliente', () => {
    entrarComo('TIENDA')
    renderizarEn('/tienda/pedidos/123')
    expect(enlacesDelMenu()).toEqual([
      'Inicio',
      'Mi tienda',
      'Solicitudes nuevas',
      'Pedidos',
      'Perfil y horario',
      'Domicilio',
      'Medios de pago',
    ])
    expect(within(menu()).getByRole('link', { name: 'Pedidos' })).toHaveAttribute('aria-current', 'page')
    expect(within(menu()).getByRole('link', { name: 'Mi tienda' })).not.toHaveAttribute('aria-current')
  })

  it('el administrador ve tiendas, reportes y catálogo', () => {
    entrarComo('ADMIN')
    renderizarEn('/')
    expect(enlacesDelMenu()).toEqual(['Inicio', 'Tiendas', 'Reportes', 'Catálogo'])
    expect(within(menu()).getByRole('link', { name: 'Inicio' })).toHaveAttribute('aria-current', 'page')
  })

  it('en celular el botón «Menú» abre y cierra el menú, y Escape lo cierra devolviendo el foco', async () => {
    const usuario = userEvent.setup({ delay: null })
    entrarComo('CLIENTE')
    renderizarEn('/')
    const boton = screen.getByRole('button', { name: 'Menú' })
    expect(boton).toHaveAttribute('aria-expanded', 'false')
    expect(boton).toHaveAttribute('aria-controls', menu().id)

    await usuario.click(boton)
    expect(boton).toHaveAttribute('aria-expanded', 'true')

    await usuario.keyboard('{Escape}')
    expect(boton).toHaveAttribute('aria-expanded', 'false')
    expect(boton).toHaveFocus()
  })

  it('al tocar un módulo en celular el menú se cierra', async () => {
    const usuario = userEvent.setup({ delay: null })
    entrarComo('CLIENTE')
    renderizarEn('/')
    const boton = screen.getByRole('button', { name: 'Menú' })
    await usuario.click(boton)
    await usuario.click(within(menu()).getByRole('link', { name: 'Mis pedidos' }))
    expect(boton).toHaveAttribute('aria-expanded', 'false')
    expect(within(menu()).getByRole('link', { name: 'Mis pedidos' })).toHaveAttribute('aria-current', 'page')
  })
})
