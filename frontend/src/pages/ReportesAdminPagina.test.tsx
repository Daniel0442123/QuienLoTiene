import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CODIGO_TOTP_DE_PRUEBA, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA, TIENDAS_SPRINT_2 } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { esErrorApi } from '@/services/errores'
import { iniciarSesion, limpiarSesion, verificarSegundoFactor } from '@/services/sesion'
import { ReportesAdminPagina } from './ReportesAdminPagina'

async function entrarComoAdministrador() {
  limpiarSesion()
  await iniciarSesion(CORREOS_DE_PRUEBA.administrador, FRASE_DE_PRUEBA)
  await verificarSegundoFactor(CODIGO_TOTP_DE_PRUEBA)
}

const la5 = () => base.tiendas.find((t) => t.id === TIENDAS_SPRINT_2.la5)!
const tarjetas = async () => screen.findAllByRole('article')
const primera = async () => within((await tarjetas())[0]!)

describe('HU-037 · Reportes y suspender tiendas · CP-ADM-037-01, CP-ADM-037-02', () => {
  beforeEach(entrarComoAdministrador)

  it('no tiene violaciones de accesibilidad y muestra los abiertos del más antiguo al más reciente', async () => {
    // Cuando: hay dos reportes abiertos de Droguería La 5
    const { container } = renderizarConProveedores(<ReportesAdminPagina />, { ruta: '/admin/reportes' })

    // Entonces
    const [vieja, nueva] = await tarjetas()
    expect(within(vieja!).getByRole('heading', { name: 'Demora en la entrega' })).toBeInTheDocument()
    expect(within(nueva!).getByRole('heading', { name: 'Cobro diferente' })).toBeInTheDocument()
    expect(within(vieja!).getByText('Abierto')).toBeInTheDocument()
    expect(within(vieja!).getByText(/^Reportado por un cliente · .* · Pedido E1F2A3B4$/)).toBeInTheDocument()
    expect(await within(vieja!).findByText('Droguería La 5')).toBeInTheDocument()
    expect(within(vieja!).getByText('2 reportes en total')).toBeInTheDocument()
    expect(within(vieja!).getByText('«Ya pasó una hora y no ha llegado.»')).toBeInTheDocument()
    expect(within(nueva!).getByText('Sin descripción.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Reportes' })).toHaveAttribute('aria-current', 'page')
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-ADM-037-02 · resuelve un reporte con nota: sale de los abiertos y queda Resuelto con su nota', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    renderizarConProveedores(<ReportesAdminPagina />)
    const tarjeta = await primera()

    // Cuando
    await usuario.type(
      tarjeta.getByRole('textbox', { name: 'Nota de resolución' }),
      'Hablé con la tienda: el domiciliario tuvo un problema.',
    )
    await usuario.click(tarjeta.getByRole('button', { name: 'Marcar como resuelto' }))

    // Entonces
    expect(await screen.findByText('Reporte resuelto. Salió de los abiertos.')).toBeInTheDocument()
    await vi.waitFor(async () => expect(await tarjetas()).toHaveLength(1))
    expect(base.reportes.find((r) => r.motivo === 'DEMORA_ENTREGA')).toMatchObject({
      estado: 'RESUELTO',
      notaResolucion: 'Hablé con la tienda: el domiciliario tuvo un problema.',
    })

    // Y en «Resueltos» se ve con su nota
    await usuario.click(screen.getByRole('radio', { name: 'Resueltos' }))
    expect(
      await screen.findByText('Nota: «Hablé con la tienda: el domiciliario tuvo un problema.»'),
    ).toBeInTheDocument()
    expect(screen.getByText('Resuelto')).toBeInTheDocument()
  })

  it('la nota debe tener al menos 3 caracteres', async () => {
    const usuario = userEvent.setup({ delay: null })
    renderizarConProveedores(<ReportesAdminPagina />)
    const tarjeta = await primera()
    await usuario.type(tarjeta.getByRole('textbox', { name: 'Nota de resolución' }), 'ok')
    await usuario.click(tarjeta.getByRole('button', { name: 'Marcar como resuelto' }))
    expect(await tarjeta.findByText('Escribe al menos 3 caracteres.')).toBeInTheDocument()
    expect(base.reportes.every((r) => r.estado === 'ABIERTO')).toBe(true)
  })

  it('REP-005 · si otra persona ya lo resolvió, lo dice y actualiza la lista', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    renderizarConProveedores(<ReportesAdminPagina />)
    const tarjeta = await primera()
    await usuario.type(tarjeta.getByRole('textbox', { name: 'Nota de resolución' }), 'Ya está resuelto')
    Object.assign(base.reportes[0]!, { estado: 'RESUELTO', notaResolucion: 'Lo resolvió otra persona' })

    // Cuando
    await usuario.click(tarjeta.getByRole('button', { name: 'Marcar como resuelto' }))

    // Entonces
    expect(await screen.findByText('Este reporte ya estaba resuelto. Actualizamos la lista.')).toBeInTheDocument()
    await vi.waitFor(async () => expect(await tarjetas()).toHaveLength(1))
  })

  it('CP-ADM-037-01 · suspender pide confirmación con motivo; la tienda queda suspendida y su dueño no entra', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    renderizarConProveedores(<ReportesAdminPagina />)
    const tarjeta = await primera()

    // Cuando: toca suspender (todavía no pasa nada)
    await usuario.click(await tarjeta.findByRole('button', { name: 'Suspender a Droguería La 5' }))
    const motivo = tarjeta.getByRole('textbox', { name: '¿Por qué la suspendes?' })
    expect(motivo).toHaveFocus()
    expect(
      tarjeta.getByText(
        'Droguería La 5 no podrá iniciar sesión ni recibirá solicitudes, y se cierran sus sesiones abiertas. Puedes reactivarla después.',
      ),
    ).toBeInTheDocument()
    expect(la5().estado).toBe('APROBADA')

    // Cuando: escribe el motivo y confirma
    await usuario.type(motivo, 'Tres reportes de demora en una semana')
    await usuario.click(tarjeta.getByRole('button', { name: 'Sí, suspender a Droguería La 5' }))

    // Entonces
    expect(await screen.findByText('Droguería La 5 quedó suspendida.')).toBeInTheDocument()
    expect(la5().estado).toBe('SUSPENDIDA')
    expect((await screen.findAllByText('Tienda suspendida')).length).toBe(2)
    expect(await screen.findAllByRole('button', { name: 'Reactivar a Droguería La 5' })).toHaveLength(2)

    // Y el dueño ya no puede iniciar sesión (IDE-009)
    limpiarSesion()
    const fallo = await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA).catch((error: unknown) => error)
    expect(esErrorApi(fallo) && fallo.codigo).toBe('IDE-009')
  })

  it('«No, volver» cierra la confirmación sin suspender y devuelve el foco', async () => {
    const usuario = userEvent.setup({ delay: null })
    renderizarConProveedores(<ReportesAdminPagina />)
    const tarjeta = await primera()
    await usuario.click(await tarjeta.findByRole('button', { name: 'Suspender a Droguería La 5' }))
    await usuario.click(tarjeta.getByRole('button', { name: 'No, volver' }))
    await vi.waitFor(() => expect(tarjeta.getByRole('button', { name: 'Suspender a Droguería La 5' })).toHaveFocus())
    expect(la5().estado).toBe('APROBADA')
  })

  it('una tienda suspendida se reactiva con confirmación', async () => {
    // Dado
    la5().estado = 'SUSPENDIDA'
    const usuario = userEvent.setup({ delay: null })
    renderizarConProveedores(<ReportesAdminPagina />)
    const tarjeta = await primera()

    // Cuando
    await usuario.click(await tarjeta.findByRole('button', { name: 'Reactivar a Droguería La 5' }))
    expect(
      tarjeta.getByText('Droguería La 5 podrá iniciar sesión y volverá a recibir solicitudes cuando abra.'),
    ).toBeInTheDocument()
    await usuario.click(tarjeta.getByRole('button', { name: 'Sí, reactivar a Droguería La 5' }))

    // Entonces
    expect(await screen.findByText('Droguería La 5 quedó activa de nuevo.')).toBeInTheDocument()
    expect(la5().estado).toBe('APROBADA')
  })

  it('TIE-004 · si la tienda ya cambió de estado, lo dice y actualiza', async () => {
    // Dado: el formulario de suspender está abierto, pero otra persona ya la suspendió
    const usuario = userEvent.setup({ delay: null })
    renderizarConProveedores(<ReportesAdminPagina />)
    const tarjeta = await primera()
    await usuario.click(await tarjeta.findByRole('button', { name: 'Suspender a Droguería La 5' }))
    await usuario.type(tarjeta.getByRole('textbox', { name: '¿Por qué la suspendes?' }), 'Reportes repetidos')
    la5().estado = 'SUSPENDIDA'

    // Cuando
    await usuario.click(tarjeta.getByRole('button', { name: 'Sí, suspender a Droguería La 5' }))

    // Entonces
    expect(await tarjeta.findByText('La tienda ya cambió de estado. Actualizamos la información.')).toBeInTheDocument()
    expect((await screen.findAllByText('Tienda suspendida')).length).toBeGreaterThan(0)
  })

  it('el nombre y el estado de la tienda vienen en el reporte: no se piden las tiendas aparte (#74)', async () => {
    // Dado: se cuentan las peticiones a la lista de tiendas
    const pedidasLasTiendas: string[] = []
    servidorSimulado.events.on('request:start', ({ request }) => {
      if (new URL(request.url).pathname === '/api/v1/admin/tiendas') pedidasLasTiendas.push(request.url)
    })

    // Cuando
    renderizarConProveedores(<ReportesAdminPagina />)

    // Entonces
    expect((await primera()).getByText('Droguería La 5')).toBeInTheDocument()
    expect(pedidasLasTiendas).toEqual([])
    servidorSimulado.events.removeAllListeners()
  })

  it('si la tienda ya no existe (nombreTienda null), muestra su número y no ofrece suspender', async () => {
    // Dado
    base.tiendas = base.tiendas.filter((t) => t.id !== TIENDAS_SPRINT_2.la5)

    // Cuando
    renderizarConProveedores(<ReportesAdminPagina />)

    // Entonces
    const tarjeta = await primera()
    expect(tarjeta.getByText(`Tienda ${TIENDAS_SPRINT_2.la5.slice(0, 8).toUpperCase()}`)).toBeInTheDocument()
    expect(tarjeta.queryByRole('button', { name: /^Suspender a / })).not.toBeInTheDocument()
  })
})
