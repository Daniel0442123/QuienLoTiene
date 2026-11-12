import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { Route, Routes } from 'react-router'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CODIGO_TOTP_DE_PRUEBA, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion, verificarSegundoFactor } from '@/services/sesion'
import { InicioSesionPagina } from './InicioSesionPagina'
import { RevisarTiendasPagina } from './RevisarTiendasPagina'
import { SegundoFactorPagina } from './SegundoFactorPagina'

const pendiente = () => base.tiendas.find((t) => t.nombre === 'Ferretería El Tornillo')
const tarjeta = async (nombre: string) => (await screen.findByRole('article', { name: nombre })) as HTMLElement

async function entrarComoAdministrador() {
  await iniciarSesion(CORREOS_DE_PRUEBA.administrador, FRASE_DE_PRUEBA)
  await verificarSegundoFactor(CODIGO_TOTP_DE_PRUEBA)
}

describe('HU-036 · Revisar, aprobar y rechazar tiendas', () => {
  beforeEach(async () => {
    limpiarSesion()
    // Dado: el administrador entró con su segundo factor
    await entrarComoAdministrador()
  })
  afterEach(() => vi.restoreAllMocks())

  it('no tiene violaciones de accesibilidad detectables', async () => {
    const { container } = renderizarConProveedores(<RevisarTiendasPagina />)
    await tarjeta('Ferretería El Tornillo')
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('muestra las pendientes con su categoría, datos y documentos', async () => {
    // Cuando
    renderizarConProveedores(<RevisarTiendasPagina />)

    // Entonces
    const ferreteria = await tarjeta('Ferretería El Tornillo')
    expect(within(ferreteria).getByText(/Ferretería · Pendiente/)).toBeInTheDocument()
    expect(within(ferreteria).getByRole('button', { name: /Descargar Foto del local \(PDF/ })).toBeInTheDocument()
    expect(
      within(ferreteria).getByRole('button', { name: /Descargar RUT o documento del negocio/ }),
    ).toBeInTheDocument()
    expect(within(ferreteria).getByRole('link', { name: /Ver la ubicación en OpenStreetMap/ })).toHaveAttribute(
      'target',
      '_blank',
    )
    expect(screen.queryByRole('article', { name: 'Droguería La 5' })).not.toBeInTheDocument()
  })

  it('CP-ADM-036-01 · aprobar pide confirmar en la tarjeta y deja la tienda APROBADA', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<RevisarTiendasPagina />)
    const ferreteria = await tarjeta('Ferretería El Tornillo')

    // Cuando
    await usuario.click(within(ferreteria).getByRole('button', { name: 'Aprobar' }))
    const confirmar = within(ferreteria).getByRole('button', { name: 'Sí, aprobar' })
    expect(confirmar).toHaveFocus()
    expect(pendiente()?.estado).toBe('PENDIENTE')
    await usuario.click(confirmar)

    // Entonces
    expect(await screen.findByText(/Aprobaste Ferretería El Tornillo/)).toBeInTheDocument()
    expect(pendiente()?.estado).toBe('APROBADA')
    expect(await screen.findByText('No hay tiendas pendientes de revisión.')).toBeInTheDocument()
  })

  it('«Cancelar» no aprueba nada', async () => {
    const usuario = userEvent.setup()
    renderizarConProveedores(<RevisarTiendasPagina />)
    const ferreteria = await tarjeta('Ferretería El Tornillo')
    await usuario.click(within(ferreteria).getByRole('button', { name: 'Aprobar' }))
    await usuario.click(within(ferreteria).getByRole('button', { name: 'Cancelar' }))
    expect(within(ferreteria).getByRole('button', { name: 'Aprobar' })).toBeInTheDocument()
    expect(pendiente()?.estado).toBe('PENDIENTE')
  })

  it('CP-ADM-036-02 · rechaza con el motivo «Documento ilegible» y la tienda queda RECHAZADA con ese motivo', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<RevisarTiendasPagina />)
    const ferreteria = await tarjeta('Ferretería El Tornillo')

    // Cuando
    await usuario.click(within(ferreteria).getByRole('button', { name: 'Rechazar' }))
    const motivo = within(ferreteria).getByRole('textbox', { name: 'Motivo del rechazo' })
    expect(motivo).toHaveFocus()
    await usuario.type(motivo, 'Documento ilegible')
    await usuario.click(within(ferreteria).getByRole('button', { name: 'Rechazar tienda' }))

    // Entonces
    expect(await screen.findByText(/Rechazaste Ferretería El Tornillo/)).toBeInTheDocument()
    expect(pendiente()).toMatchObject({ estado: 'RECHAZADA', motivoRechazo: 'Documento ilegible' })
  })

  it('TIE-005 · no rechaza con un motivo de menos de 5 caracteres y dice cuántos lleva', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<RevisarTiendasPagina />)
    const ferreteria = await tarjeta('Ferretería El Tornillo')
    await usuario.click(within(ferreteria).getByRole('button', { name: 'Rechazar' }))

    // Cuando
    await usuario.type(within(ferreteria).getByRole('textbox', { name: 'Motivo del rechazo' }), 'mal')
    await usuario.click(within(ferreteria).getByRole('button', { name: 'Rechazar tienda' }))

    // Entonces
    expect(within(ferreteria).getByRole('textbox', { name: 'Motivo del rechazo' })).toHaveAccessibleDescription(
      /al menos 5 caracteres\. Llevas 3\./,
    )
    expect(pendiente()?.estado).toBe('PENDIENTE')
  })

  it('TIE-004 · si otra persona ya la revisó, lo avisa y actualiza la lista', async () => {
    // Dado: entre que se cargó la lista y el clic, otra persona aprobó la tienda
    const usuario = userEvent.setup()
    renderizarConProveedores(<RevisarTiendasPagina />)
    const ferreteria = await tarjeta('Ferretería El Tornillo')
    pendiente()!.estado = 'APROBADA'

    // Cuando
    await usuario.click(within(ferreteria).getByRole('button', { name: 'Aprobar' }))
    await usuario.click(within(ferreteria).getByRole('button', { name: 'Sí, aprobar' }))

    // Entonces
    expect(await screen.findByText(/Otra persona ya revisó esta tienda/)).toBeInTheDocument()
    expect(await screen.findByText('No hay tiendas pendientes de revisión.')).toBeInTheDocument()
  })

  it('descarga un documento con su nombre de archivo', async () => {
    // Dado
    const usuario = userEvent.setup()
    const crearUrl = vi.fn(() => 'blob:documento')
    Object.assign(URL, { createObjectURL: crearUrl, revokeObjectURL: vi.fn() })
    const clic = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined)
    renderizarConProveedores(<RevisarTiendasPagina />)
    const ferreteria = await tarjeta('Ferretería El Tornillo')

    // Cuando
    await usuario.click(within(ferreteria).getByRole('button', { name: /Descargar RUT/ }))

    // Entonces
    await vi.waitFor(() => expect(clic).toHaveBeenCalled())
    expect(crearUrl).toHaveBeenCalledWith(expect.any(Blob))
    expect((clic.mock.contexts[0] as HTMLAnchorElement).download).toBe('rut.pdf')
  })

  it('cambia de estado con el filtro y lo guarda en la dirección', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<RevisarTiendasPagina />)
    await tarjeta('Ferretería El Tornillo')

    // Cuando
    await usuario.click(screen.getByRole('radio', { name: 'Aprobadas' }))

    // Entonces
    expect(await tarjeta('Droguería La 5')).toBeInTheDocument()
    expect(within(await tarjeta('Droguería La 5')).queryByRole('button', { name: 'Aprobar' })).not.toBeInTheDocument()
  })

  it('abre directo un estado pedido en la dirección (?estado=APROBADA)', async () => {
    renderizarConProveedores(<RevisarTiendasPagina />, { ruta: '/?estado=APROBADA' })
    expect(await tarjeta('Droguería La 5')).toBeInTheDocument()
    expect(screen.getByRole('radio', { name: 'Aprobadas' })).toBeChecked()
  })

  it('estado vacío · sin tiendas en ese estado dice qué pasa', async () => {
    renderizarConProveedores(<RevisarTiendasPagina />, { ruta: '/?estado=SUSPENDIDA' })
    expect(await screen.findByText('No hay tiendas suspendidas.')).toBeInTheDocument()
  })

  it('estado cargando con esqueleto', async () => {
    servidorSimulado.use(http.get('/api/v1/admin/tiendas', () => delay('infinite')))
    renderizarConProveedores(<RevisarTiendasPagina />)
    expect(await screen.findByRole('status', { name: 'Cargando tiendas…' })).toBeInTheDocument()
  })

  it('estado error con reintentar', async () => {
    // Dado
    const usuario = userEvent.setup()
    let intentos = 0
    servidorSimulado.use(
      http.get('/api/v1/admin/tiendas', () => {
        intentos += 1
        if (intentos === 1) {
          return HttpResponse.json({ title: 'Error', status: 500, codigo: 'GEN-008', errores: [] }, { status: 500 })
        }
        return undefined
      }),
    )
    renderizarConProveedores(<RevisarTiendasPagina />)

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Reintentar' }, { timeout: 5000 }))

    // Entonces
    expect(await tarjeta('Ferretería El Tornillo')).toBeInTheDocument()
  })

  it('después del código, el administrador llega a revisar tiendas', async () => {
    // Dado
    limpiarSesion()
    const usuario = userEvent.setup()
    renderizarConProveedores(
      <Routes>
        <Route path="/entrar" element={<InicioSesionPagina />} />
        <Route path="/entrar/codigo" element={<SegundoFactorPagina />} />
        <Route path="/admin/tiendas" element={<RevisarTiendasPagina />} />
      </Routes>,
      { ruta: '/entrar' },
    )

    // Cuando
    await usuario.type(screen.getByRole('textbox', { name: 'Correo' }), CORREOS_DE_PRUEBA.administrador)
    await usuario.type(screen.getByLabelText('Frase de contraseña'), FRASE_DE_PRUEBA)
    await usuario.click(screen.getByRole('button', { name: 'Entrar' }))
    await usuario.type(await screen.findByRole('textbox', { name: 'Código de 6 dígitos' }), CODIGO_TOTP_DE_PRUEBA)
    await usuario.click(screen.getByRole('button', { name: 'Verificar' }))

    // Entonces
    expect(await screen.findByRole('heading', { level: 1, name: 'Revisar tiendas' })).toBeInTheDocument()
  })
})
