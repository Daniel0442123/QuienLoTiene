import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA, TIENDAS_SPRINT_2 } from '@/mocks/datos'
import { ConfirmarPedidoPagina } from '@/pages/ConfirmarPedidoPagina'
import { PublicarSolicitudPagina } from '@/pages/PublicarSolicitudPagina'
import { SolicitudClientePagina } from '@/pages/SolicitudClientePagina'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { crearSolicitud } from '@/pruebas/solicitudes'
import { clienteApi } from '@/services/clienteApi'
import { esErrorApi } from '@/services/errores'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'

vi.mock('@/components/MapaUbicacion', () => ({ default: () => null }))

const AVISO = 'Este medicamento necesita fórmula médica'
const campoProducto = () => screen.findByRole('textbox', { name: '¿Qué estás buscando?' })

function abrir(ruta: string) {
  return renderizarConProveedores(
    <Routes>
      <Route path="/solicitudes/nueva" element={<PublicarSolicitudPagina />} />
      <Route path="/solicitudes/:solicitudId" element={<SolicitudClientePagina />} />
      <Route path="/solicitudes/:solicitudId/elegir/:respuestaId" element={<ConfirmarPedidoPagina />} />
    </Routes>,
    { ruta },
  )
}

describe('HU-023 · Aviso de producto restringido · CP-SOL-023-01, CP-SOL-023-02', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })

  it('CP-SOL-023-01 · al escribir «Amoxicilina 500 mg» avisa antes de publicar y no ofrece domicilio', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    const { container } = abrir('/solicitudes/nueva')

    // Cuando
    await usuario.type(await campoProducto(), 'Amoxicilina 500 mg')

    // Entonces: el aviso, asociado al campo y anunciado; domicilio deshabilitado y explicado
    const aviso = await screen.findByText(AVISO)
    expect(
      screen.getByText('Las tiendas no lo llevan a domicilio: recógelo en la tienda con tu fórmula médica.'),
    ).toBeInTheDocument()
    expect((await campoProducto()).getAttribute('aria-describedby')).toContain(aviso.closest('[id]')!.id)
    expect(screen.getByRole('radio', { name: 'Domicilio (no disponible: necesita fórmula médica)' })).toBeDisabled()
    expect(screen.getByRole('radio', { name: 'Lo recojo en la tienda' })).toBeChecked()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])

    // Cuando: publica
    await usuario.click(await screen.findByRole('radio', { name: 'Farmacia' }))
    await usuario.click(screen.getByRole('button', { name: 'Preguntar' }))

    // Entonces: la solicitud queda marcada y la pantalla siguiente lo sigue diciendo
    expect(await screen.findByRole('heading', { level: 1, name: 'Esperando respuestas' })).toBeInTheDocument()
    expect(screen.getByText(AVISO)).toBeInTheDocument()
    expect(base.solicitudes.at(-1)).toMatchObject({ producto: 'Amoxicilina 500 mg', esRestringida: true })
  })

  it('no distingue mayúsculas ni tildes, y un producto común no muestra el aviso', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    abrir('/solicitudes/nueva')
    const campo = await campoProducto()

    // Cuando: un producto común
    await usuario.type(campo, 'Acetaminofén 500 mg')

    // Entonces
    await vi.waitFor(() => expect(screen.getByRole('radio', { name: 'Domicilio' })).toBeEnabled())
    // Se deja pasar la espera de la consulta (500 ms, useValorDiferido) para comprobar que no llega el aviso
    await new Promise((listo) => setTimeout(listo, 700))
    expect(screen.queryByText(AVISO)).not.toBeInTheDocument()

    // Cuando: uno restringido escrito en mayúsculas y con tilde
    await usuario.clear(campo)
    await usuario.type(campo, 'TRAMADÓL gotas')

    // Entonces
    expect(await screen.findByText(AVISO)).toBeInTheDocument()
  })

  it('si había elegido domicilio y escribe un restringido, pasa a recoger', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    abrir('/solicitudes/nueva')
    const campo = await campoProducto()
    await usuario.click(screen.getByRole('radio', { name: 'Domicilio' }))

    // Cuando
    await usuario.type(campo, 'Clonazepam')

    // Entonces
    expect(await screen.findByText(AVISO)).toBeInTheDocument()
    expect(screen.getByRole('radio', { name: 'Lo recojo en la tienda' })).toBeChecked()
  })

  it('CP-SOL-023-02 · al confirmar el pedido de una solicitud restringida no se ofrece domicilio', async () => {
    // Dado: la clienta pidió a domicilio un producto restringido y Botica respondió (sin domicilio)
    const solicitudId = crearSolicitud({ producto: 'Amoxicilina 500 mg', modalidad: 'DOMICILIO', esRestringida: true })
    const respuesta = base.respuestas.find(
      (r) => r.solicitudId === solicitudId && r.tiendaId === TIENDAS_SPRINT_2.botica,
    )!
    expect(respuesta.ofreceDomicilio).toBe(false)

    // Cuando
    abrir(`/solicitudes/${solicitudId}/elegir/${respuesta.id}`)

    // Entonces
    expect(
      await screen.findByText('Este medicamento necesita fórmula médica: solo se puede recoger en la tienda.'),
    ).toBeInTheDocument()
    expect(screen.queryByRole('radio', { name: /^Domicilio/ })).not.toBeInTheDocument()
    expect(screen.getByRole('radio', { name: 'Recoger en la tienda' })).toBeChecked()
  })

  it('CP-SOL-023-02 · la API rechaza el domicilio con ORD-006 y la pantalla tiene su mensaje', async () => {
    // Dado
    const solicitudId = crearSolicitud({ producto: 'Amoxicilina 500 mg', esRestringida: true })
    const respuesta = base.respuestas.find(
      (r) => r.solicitudId === solicitudId && r.tiendaId === TIENDAS_SPRINT_2.botica,
    )!
    const casa = base.direcciones.find((d) => d.clienteId === base.solicitudes.at(-1)!.clienteId)!

    // Cuando
    const fallo = await clienteApi
      .POST('/api/v1/ordenes', {
        body: { respuestaId: respuesta.id, modalidad: 'DOMICILIO', direccionId: casa.id, medioPago: 'EFECTIVO' },
      })
      .catch((error: unknown) => error)

    // Entonces
    expect(esErrorApi(fallo) && fallo.codigo).toBe('ORD-006')
    expect(base.ordenes.some((o) => o.respuestaId === respuesta.id)).toBe(false)
  })

  it('la solicitud restringida dice el aviso también en sus respuestas', async () => {
    const solicitudId = crearSolicitud({ producto: 'Tramadol', esRestringida: true })
    abrir(`/solicitudes/${solicitudId}`)
    expect(await screen.findByText(AVISO)).toBeInTheDocument()
    expect(await screen.findAllByText(/Solo para recoger/)).not.toHaveLength(0)
  })
})
