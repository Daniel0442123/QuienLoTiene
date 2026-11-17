import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { MisPedidosPagina } from './MisPedidosPagina'
import { PedidoPagina } from './PedidoPagina'

const ORDEN_DE_LA_CLIENTA = 'e1f2a3b4-0002-4c5d-8e9f-0a1b2c3d4e02'
const ORDEN_DE_LA_VECINA = 'e1f2a3b4-0001-4c5d-8e9f-0a1b2c3d4e01'

function abrir(ruta: string) {
  return renderizarConProveedores(
    <Routes>
      <Route path="/pedidos" element={<MisPedidosPagina />} />
      <Route path="/pedidos/:ordenId" element={<PedidoPagina />} />
    </Routes>,
    { ruta },
  )
}

describe('HU-027, HU-028 y HU-032 · El pedido del cliente · CP-ORD-028-01, CP-ORD-028-02, CP-MEN-032-01', () => {
  beforeEach(async () => {
    limpiarSesion()
    // Dado: la clienta tiene un pedido para recoger que la tienda ya marcó como entregado
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })

  it('no tiene violaciones de accesibilidad detectables y marca el paso actual en texto', async () => {
    // Cuando
    const { container } = abrir(`/pedidos/${ORDEN_DE_LA_CLIENTA}`)

    // Entonces
    const actual = (await screen.findAllByRole('listitem')).find((paso) => paso.getAttribute('aria-current') === 'step')
    expect(actual).toHaveTextContent(/Entregado · .*\(estado actual\)/)
    expect(screen.getByText('Pedido E1F2A3B4')).toBeInTheDocument()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-MEN-032-01 · con el pedido activo ofrece WhatsApp de la tienda, que abre wa.me', async () => {
    // Cuando
    abrir(`/pedidos/${ORDEN_DE_LA_CLIENTA}`)

    // Entonces
    const enlace = await screen.findByRole('link', { name: /Escribir por WhatsApp a Droguería La 5/ })
    expect(enlace).toHaveAttribute('href', expect.stringMatching(/^https:\/\/wa\.me\/573001234567\?text=/))
    expect(enlace).toHaveAttribute('target', '_blank')
    expect(enlace).toHaveAttribute('rel', 'noopener noreferrer')
  })

  it('CP-ORD-028-01 · «Recibí mi pedido» lo deja recibido, quita WhatsApp y la tienda deja de ver los datos', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    abrir(`/pedidos/${ORDEN_DE_LA_CLIENTA}`)
    expect(
      await screen.findByText(/Droguería La 5 marcó tu pedido como entregado a las .*\. ¿Ya lo tienes\?/),
    ).toBeInTheDocument()

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'Recibí mi pedido' }))

    // Entonces
    expect(await screen.findByText('Recibido', { selector: 'p' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Recibí mi pedido' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /WhatsApp/ })).not.toBeInTheDocument()
    expect(base.ordenes.find((o) => o.id === ORDEN_DE_LA_CLIENTA)?.estado).toBe('RESUELTA')
  })

  it('CP-ORD-028-02 · el pedido de otro cliente no se muestra', async () => {
    // Cuando
    abrir(`/pedidos/${ORDEN_DE_LA_VECINA}`)

    // Entonces
    expect(await screen.findByText('No encontramos ese pedido.')).toBeInTheDocument()
    expect(screen.queryByText('Loratadina 10 mg', { exact: false })).not.toBeInTheDocument()
  })

  it('HU-027 · «Mis pedidos» muestra los que están en curso con su estado y lleva al detalle', async () => {
    // Cuando
    const { container } = abrir('/pedidos')

    // Entonces
    const enCurso = await screen.findByRole('region', { name: 'En curso' })
    const enlace = within(enCurso).getByRole('link', { name: /Ibuprofeno 400 mg/ })
    expect(enlace).toHaveTextContent('Entregado')
    expect(enlace).toHaveAttribute('href', `/pedidos/${ORDEN_DE_LA_CLIENTA}`)
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })
})
