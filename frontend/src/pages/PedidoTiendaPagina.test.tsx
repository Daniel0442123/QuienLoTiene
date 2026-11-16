import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { PedidosTiendaPagina } from './PedidosTiendaPagina'
import { PedidoTiendaPagina } from './PedidoTiendaPagina'

const ORDEN_DE_LA_VECINA = 'e1f2a3b4-0001-4c5d-8e9f-0a1b2c3d4e01'
const ordenEnBase = () => base.ordenes.find((o) => o.id === ORDEN_DE_LA_VECINA)!

function abrir(ruta = `/tienda/pedidos/${ORDEN_DE_LA_VECINA}`) {
  return renderizarConProveedores(
    <Routes>
      <Route path="/tienda/pedidos" element={<PedidosTiendaPagina />} />
      <Route path="/tienda/pedidos/:ordenId" element={<PedidoTiendaPagina />} />
    </Routes>,
    { ruta },
  )
}

describe('HU-027 y HU-032 · El pedido de la tienda · CP-ORD-024-02, CP-ORD-027-01, CP-ORD-027-02', () => {
  beforeEach(async () => {
    limpiarSesion()
    // Dado: la tienda de prueba tiene un pedido CONFIRMADO con domicilio de la vecina
    await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA)
  })

  it('no tiene violaciones de accesibilidad detectables', async () => {
    const { container } = abrir()
    await screen.findByRole('button', { name: 'Empezar a preparar' })
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-ORD-024-02 · la tienda elegida ve a quién y a dónde entregar, con el teléfono de la dirección', async () => {
    // Cuando
    abrir()

    // Entonces
    const entrega = await screen.findByRole('region', { name: 'Entregar a' })
    expect(entrega).toHaveTextContent('Vecina de Prueba')
    expect(entrega).toHaveTextContent('Carrera 9 # 4-15, apto 302')
    expect(screen.getByRole('link', { name: '3168889900' })).toHaveAttribute('href', 'tel:3168889900')
    expect(await screen.findByRole('link', { name: /Escribir por WhatsApp a Vecina de Prueba/ })).toHaveAttribute(
      'href',
      expect.stringMatching(/^https:\/\/wa\.me\/573168889900\?text=/),
    )
  })

  it('CP-ORD-027-01 · ofrece solo el paso siguiente y al tocarlo avanza el pedido', async () => {
    // Dado
    const usuario = userEvent.setup()
    abrir()

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Empezar a preparar' }))

    // Entonces
    expect(await screen.findByRole('button', { name: 'Salió a domicilio' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Marcar como entregado' })).not.toBeInTheDocument()
    expect(ordenEnBase().estado).toBe('PREPARANDO')
  })

  it('CP-ORD-027-02 · si el pedido ya cambió en otro celular, lo dice y muestra el estado real', async () => {
    // Dado
    const usuario = userEvent.setup()
    abrir()
    const boton = await screen.findByRole('button', { name: 'Empezar a preparar' })
    ordenEnBase().estado = 'PREPARANDO'

    // Cuando
    await usuario.click(boton)

    // Entonces
    expect(
      await screen.findByText('El pedido no puede pasar a ese estado ahora. Recarga para ver cómo va.'),
    ).toBeInTheDocument()
    expect(await screen.findByRole('button', { name: 'Salió a domicilio' })).toBeInTheDocument()
  })

  it('con el pedido cerrado no muestra los datos del cliente, ni WhatsApp, ni pasos para dar', async () => {
    // Dado
    ordenEnBase().estado = 'RESUELTA'

    // Cuando
    abrir()

    // Entonces
    expect(
      await screen.findByText('Los datos del cliente ya no se muestran porque el pedido se cerró.'),
    ).toBeInTheDocument()
    expect(screen.queryByText('Vecina de Prueba')).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /WhatsApp/ })).not.toBeInTheDocument()
    expect(screen.queryByRole('button')).not.toBeInTheDocument()
  })

  it('la lista de pedidos de la tienda muestra el cliente y el estado de cada uno', async () => {
    // Cuando
    abrir('/tienda/pedidos')

    // Entonces
    const enlace = await screen.findByRole('link', { name: /Loratadina 10 mg/ })
    expect(enlace).toHaveTextContent('Vecina de Prueba')
    expect(enlace).toHaveTextContent('Confirmado')
    expect(enlace).toHaveAttribute('href', `/tienda/pedidos/${ORDEN_DE_LA_VECINA}`)
  })
})
