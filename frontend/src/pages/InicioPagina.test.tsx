import { screen, within } from '@testing-library/react'
import { beforeEach, describe, expect, it } from 'vitest'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { guardarSesion, limpiarSesion, type Rol } from '@/services/sesion'
import { InicioPagina } from './InicioPagina'

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

describe('SC-020 · Página de inicio', () => {
  beforeEach(() => limpiarSesion())

  it('sin sesión explica la app en tres pasos y ofrece crear cuenta, entrar y registrar la tienda', async () => {
    const { container } = renderizarConProveedores(<InicioPagina />)
    expect(screen.getByRole('heading', { level: 1, name: '¿Quién lo tiene?' })).toBeInTheDocument()
    const pasos = within(screen.getByRole('region', { name: 'Cómo funciona' })).getAllByRole('listitem')
    expect(pasos).toHaveLength(3)
    expect(pasos[2]).toHaveTextContent('pides domicilio')
    expect(screen.getByRole('link', { name: 'Crear una cuenta' })).toHaveAttribute('href', '/crear-cuenta')
    expect(screen.getByRole('link', { name: 'Entrar' })).toHaveAttribute('href', '/entrar')
    expect(screen.getByRole('link', { name: 'Registra tu tienda' })).toHaveAttribute('href', '/registrar-tienda')
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('el cliente ve el saludo, «Preguntar quién lo tiene» y accesos a sus preguntas, pedidos y cuenta', async () => {
    entrarComo('CLIENTE')
    const { container } = renderizarConProveedores(<InicioPagina />)
    expect(screen.getByRole('heading', { level: 1, name: '¿Qué necesitas hoy?' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Preguntar quién lo tiene' })).toHaveAttribute('href', '/solicitudes/nueva')
    const accesos = within(screen.getByRole('region', { name: 'Accesos rápidos' }))
    expect(accesos.getByRole('link', { name: /Mis preguntas/ })).toHaveAttribute('href', '/solicitudes')
    expect(accesos.getByRole('link', { name: /Mis pedidos/ })).toHaveAttribute('href', '/pedidos')
    expect(accesos.getByRole('link', { name: /Mi cuenta/ })).toHaveAttribute('href', '/cuenta')
    expect(screen.queryByRole('link', { name: 'Crear una cuenta' })).not.toBeInTheDocument()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('la tienda ve el acceso a su panel, a las solicitudes nuevas y a sus pedidos', () => {
    entrarComo('TIENDA')
    renderizarConProveedores(<InicioPagina />)
    expect(screen.getByRole('link', { name: 'Ir al panel de mi tienda' })).toHaveAttribute('href', '/tienda')
    const accesos = within(screen.getByRole('region', { name: 'Accesos rápidos' }))
    expect(accesos.getByRole('link', { name: /Solicitudes nuevas/ })).toHaveAttribute('href', '/tienda/solicitudes')
    expect(accesos.getByRole('link', { name: /Pedidos/ })).toHaveAttribute('href', '/tienda/pedidos')
  })

  it('el administrador ve accesos a tiendas, reportes y catálogo', () => {
    entrarComo('ADMIN')
    renderizarConProveedores(<InicioPagina />)
    expect(screen.getByRole('heading', { level: 1, name: 'Administración' })).toBeInTheDocument()
    const accesos = within(screen.getByRole('region', { name: 'Accesos rápidos' }))
    expect(accesos.getAllByRole('link').map((enlace) => enlace.getAttribute('href'))).toEqual([
      '/admin/tiendas',
      '/admin/reportes',
      '/admin/catalogo',
    ])
  })
})
