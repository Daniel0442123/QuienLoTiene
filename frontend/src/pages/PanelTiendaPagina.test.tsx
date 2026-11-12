import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { InicioSesionPagina } from './InicioSesionPagina'
import { PanelTiendaPagina } from './PanelTiendaPagina'

const interruptor = () => screen.getByRole('switch', { name: 'Recibir solicitudes' })
const tiendaDe = (correo: string) => {
  const dueno = base.usuarios.find((u) => u.correo === correo)
  return base.tiendas.find((t) => t.duenoId === dueno?.id)
}

describe('HU-013 · Panel de la tienda y abrir o cerrar', () => {
  beforeEach(async () => {
    limpiarSesion()
    // Dado: el dueño de una tienda aprobada y abierta inició sesión
    await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA)
  })

  it('no tiene violaciones de accesibilidad detectables', async () => {
    const { container } = renderizarConProveedores(<PanelTiendaPagina />)
    await screen.findByRole('switch')
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('muestra la tienda, su estado en texto y las secciones de configuración', async () => {
    // Cuando
    renderizarConProveedores(<PanelTiendaPagina />)

    // Entonces
    expect(await screen.findByRole('heading', { name: 'Droguería La 5' })).toBeInTheDocument()
    expect(screen.getByText('Aprobada')).toBeInTheDocument()
    const secciones = screen.getByRole('navigation', { name: 'Secciones de mi tienda' })
    expect(secciones).toHaveTextContent('Perfil y horario')
    expect(secciones).toHaveTextContent('Domicilio')
    expect(secciones).toHaveTextContent('Medios de pago')
  })

  it('CP-TIE-013-01 · con un toque cierra la tienda, lo dice en texto y lo anuncia', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<PanelTiendaPagina />)
    expect(await screen.findByRole('switch')).toHaveAttribute('aria-checked', 'true')

    // Cuando
    await usuario.click(interruptor())

    // Entonces
    expect(interruptor()).toHaveAttribute('aria-checked', 'false')
    expect(interruptor()).toHaveTextContent('Cerrada')
    await vi.waitFor(() => expect(tiendaDe(CORREOS_DE_PRUEBA.tienda)?.estaAbierta).toBe(false))
    await vi.waitFor(() =>
      expect(screen.getByText('Tienda cerrada. No recibes solicitudes nuevas.')).toBeInTheDocument(),
    )
  })

  it('CP-TIE-013-02 · con otro toque la vuelve a abrir', async () => {
    // Dado
    const usuario = userEvent.setup()
    tiendaDe(CORREOS_DE_PRUEBA.tienda)!.estaAbierta = false
    renderizarConProveedores(<PanelTiendaPagina />)
    await screen.findByRole('switch')

    // Cuando
    await usuario.click(interruptor())

    // Entonces
    await vi.waitFor(() => expect(tiendaDe(CORREOS_DE_PRUEBA.tienda)?.estaAbierta).toBe(true))
    expect(interruptor()).toHaveTextContent('Abierta')
  })

  it('TIE-008 · si el backend lo rechaza, vuelve al estado anterior y explica por qué', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(
      http.patch('/api/v1/tiendas/me/apertura', () =>
        HttpResponse.json(
          { title: 'Tienda no aprobada', status: 409, codigo: 'TIE-008', errores: [] },
          { status: 409 },
        ),
      ),
    )
    renderizarConProveedores(<PanelTiendaPagina />)
    await screen.findByRole('switch')

    // Cuando
    await usuario.click(interruptor())

    // Entonces
    await vi.waitFor(() => expect(interruptor()).toHaveAttribute('aria-checked', 'true'))
    expect(interruptor()).toHaveAccessibleDescription(/aún no está aprobada/)
  })

  it('estado deshabilitado · la tienda en revisión no puede abrir y ve la razón', async () => {
    // Dado
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.tiendaPendiente, FRASE_DE_PRUEBA)

    // Cuando
    renderizarConProveedores(<PanelTiendaPagina />)

    // Entonces
    expect(await screen.findByRole('switch')).toBeDisabled()
    expect(interruptor()).toHaveAccessibleDescription(/cuando el equipo la apruebe/)
    expect(screen.getByText('En revisión')).toBeInTheDocument()
  })

  it('la tienda rechazada ve el motivo', async () => {
    // Dado
    limpiarSesion()
    const tienda = tiendaDe(CORREOS_DE_PRUEBA.tiendaPendiente)!
    Object.assign(tienda, { estado: 'RECHAZADA', motivoRechazo: 'El RUT no se lee' })
    await iniciarSesion(CORREOS_DE_PRUEBA.tiendaPendiente, FRASE_DE_PRUEBA)

    // Cuando
    renderizarConProveedores(<PanelTiendaPagina />)

    // Entonces
    expect(await screen.findByText('Motivo: El RUT no se lee')).toBeInTheDocument()
  })

  it('estado cargando con esqueleto', async () => {
    servidorSimulado.use(http.get('/api/v1/tiendas/me', () => delay('infinite')))
    renderizarConProveedores(<PanelTiendaPagina />)
    expect(await screen.findByRole('status', { name: 'Cargando tu tienda…' })).toBeInTheDocument()
  })

  it('estado error con reintentar', async () => {
    // Dado
    const usuario = userEvent.setup()
    let intentos = 0
    servidorSimulado.use(
      http.get('/api/v1/tiendas/me', () => {
        intentos += 1
        if (intentos === 1) {
          return HttpResponse.json({ title: 'Error', status: 500, codigo: 'GEN-008', errores: [] }, { status: 500 })
        }
        return undefined
      }),
    )
    renderizarConProveedores(<PanelTiendaPagina />)

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Reintentar' }, { timeout: 5000 }))

    // Entonces
    expect(await screen.findByRole('heading', { name: 'Droguería La 5' })).toBeInTheDocument()
  })

  it('al entrar, el dueño de la tienda llega a su panel', async () => {
    // Dado
    limpiarSesion()
    const usuario = userEvent.setup()
    renderizarConProveedores(
      <Routes>
        <Route path="/entrar" element={<InicioSesionPagina />} />
        <Route path="/tienda" element={<PanelTiendaPagina />} />
      </Routes>,
      { ruta: '/entrar' },
    )

    // Cuando
    await usuario.type(screen.getByRole('textbox', { name: 'Correo' }), CORREOS_DE_PRUEBA.tienda)
    await usuario.type(screen.getByLabelText('Frase de contraseña'), FRASE_DE_PRUEBA)
    await usuario.click(screen.getByRole('button', { name: 'Entrar' }))

    // Entonces
    expect(await screen.findByRole('heading', { level: 1, name: 'Mi tienda' })).toBeInTheDocument()
  })
})
