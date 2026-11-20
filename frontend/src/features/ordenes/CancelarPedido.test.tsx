import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it } from 'vitest'
import { CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import {
  abrirPedidoCliente,
  abrirPedidoTienda,
  ORDEN_DE_LA_CLIENTA,
  ORDEN_DE_LA_VECINA,
  ordenEnBase,
  ponerOrdenEn,
} from '@/pruebas/ordenes'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'

const botonCancelar = () => screen.findByRole('button', { name: 'Cancelar pedido' })
const campoMotivo = () => screen.getByRole('textbox', { name: '¿Por qué cancelas el pedido?' })
const confirmar = () => screen.getByRole('button', { name: 'Sí, cancelar el pedido' })
/** El paso marcado como actual en la línea de tiempo. */
const pasoActual = async () =>
  (await screen.findAllByRole('listitem')).find((paso) => paso.getAttribute('aria-current') === 'step')

async function entrarComo(correo: string) {
  limpiarSesion()
  await iniciarSesion(correo, FRASE_DE_PRUEBA)
}

describe('HU-029 · Cancelar el pedido · CP-ORD-029-01, CP-ORD-029-02', () => {
  beforeEach(() => entrarComo(CORREOS_DE_PRUEBA.cliente))

  it('no tiene violaciones de accesibilidad con el formulario abierto y con errores', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'PREPARANDO')
    const { container } = abrirPedidoCliente(ORDEN_DE_LA_CLIENTA)

    // Cuando
    await usuario.click(await botonCancelar())
    await usuario.click(confirmar())

    // Entonces
    expect(await screen.findByText('Escribe por qué cancelas.')).toBeInTheDocument()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-ORD-029-01 · el cliente cancela en Preparando con su motivo y la tienda lo ve sin los datos del cliente', async () => {
    // Dado: la clienta tiene un pedido que La 5 ya está preparando
    const usuario = userEvent.setup({ delay: null })
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'PREPARANDO')
    const { unmount } = abrirPedidoCliente(ORDEN_DE_LA_CLIENTA)

    // Cuando: abre el formulario, escribe el motivo y confirma
    await usuario.click(await botonCancelar())
    expect(campoMotivo()).toHaveFocus()
    expect(screen.getByText('La tienda deja de preparar tu pedido. No se puede deshacer.')).toBeInTheDocument()
    await usuario.type(campoMotivo(), 'Ya lo conseguí en otra parte')
    await usuario.click(confirmar())

    // Entonces: queda Cancelado, con quién, cuándo y por qué, y sin el botón
    expect(await screen.findByText(/^A las .*, cancelaste el pedido\.$/)).toBeInTheDocument()
    expect(screen.getByText('Motivo: «Ya lo conseguí en otra parte»')).toBeInTheDocument()
    expect(screen.getByText(/^Cancelado por el cliente · /)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Cancelar pedido' })).not.toBeInTheDocument()
    expect(ordenEnBase(ORDEN_DE_LA_CLIENTA)).toMatchObject({
      estado: 'CANCELADO',
      canceladaPor: 'CLIENTE',
      motivoCancelacion: 'Ya lo conseguí en otra parte',
    })

    // Y la tienda: lo ve cancelado, con el motivo, y ya no ve los datos del cliente (SC-011 punto 9)
    unmount()
    await entrarComo(CORREOS_DE_PRUEBA.tienda)
    abrirPedidoTienda(ORDEN_DE_LA_CLIENTA)
    expect(await screen.findByText(/^A las .*, el cliente canceló el pedido\.$/)).toBeInTheDocument()
    expect(screen.getByText('Motivo: «Ya lo conseguí en otra parte»')).toBeInTheDocument()
    expect(screen.getByText('No lo prepares ni lo envíes.')).toBeInTheDocument()
    expect(screen.getByText('Los datos del cliente ya no se muestran porque el pedido se cerró.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Marcar como entregado|Listo para recoger/ })).not.toBeInTheDocument()
  })

  it('también puede cancelar una recogida que ya está lista (todavía no salió de la tienda)', async () => {
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'LISTO_PARA_RECOGER')
    abrirPedidoCliente(ORDEN_DE_LA_CLIENTA)
    expect(await botonCancelar()).toBeInTheDocument()
  })

  it('CP-ORD-029-02 · con el pedido en camino no ofrece cancelar', async () => {
    // Dado: el pedido de la vecina ya salió a domicilio
    await entrarComo(CORREOS_DE_PRUEBA.vecina)
    ponerOrdenEn(ORDEN_DE_LA_VECINA, 'EN_CAMINO')

    // Cuando
    abrirPedidoCliente(ORDEN_DE_LA_VECINA)

    // Entonces
    expect(await pasoActual()).toHaveTextContent(/En camino · .*\(estado actual\)/)
    expect(screen.queryByRole('button', { name: 'Cancelar pedido' })).not.toBeInTheDocument()
  })

  it('CP-ORD-029-02 · si salió mientras escribía el motivo, ORD-005 dice que escriba a la tienda o reporte', async () => {
    // Dado: la vecina abre el formulario con el pedido en Preparando
    const usuario = userEvent.setup({ delay: null })
    await entrarComo(CORREOS_DE_PRUEBA.vecina)
    ponerOrdenEn(ORDEN_DE_LA_VECINA, 'PREPARANDO')
    abrirPedidoCliente(ORDEN_DE_LA_VECINA)
    await usuario.click(await botonCancelar())
    await usuario.type(campoMotivo(), 'Me demoré en decidir')

    // Cuando: la tienda lo despacha antes de que confirme
    ponerOrdenEn(ORDEN_DE_LA_VECINA, 'EN_CAMINO')
    await usuario.click(confirmar())

    // Entonces: el mensaje dice qué pasó y cómo seguir, y ya no se ofrece cancelar
    expect(
      await screen.findByText(
        'Tu pedido ya salió de la tienda, así que ya no se puede cancelar. Escríbele a la tienda o reporta un problema.',
      ),
    ).toBeInTheDocument()
    await expect.poll(async () => (await pasoActual())?.textContent).toMatch(/En camino · .*\(estado actual\)/)
    expect(screen.queryByRole('button', { name: 'Cancelar pedido' })).not.toBeInTheDocument()
    expect(await screen.findByRole('link', { name: /Escribir por WhatsApp a Droguería La 5/ })).toBeInTheDocument()
    expect(ordenEnBase(ORDEN_DE_LA_VECINA).estado).toBe('EN_CAMINO')
  })

  it('valida el motivo: obligatorio y de al menos 3 caracteres', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'CONFIRMADO')
    abrirPedidoCliente(ORDEN_DE_LA_CLIENTA)
    await usuario.click(await botonCancelar())

    // Cuando: vacío
    await usuario.click(confirmar())

    // Entonces
    expect(await screen.findByText('Escribe por qué cancelas.')).toBeInTheDocument()
    expect(campoMotivo()).toHaveAttribute('aria-invalid', 'true')

    // Cuando: muy corto
    await usuario.type(campoMotivo(), 'no')
    await usuario.click(confirmar())

    // Entonces: no se canceló
    expect(await screen.findByText('Escribe al menos 3 caracteres.')).toBeInTheDocument()
    expect(ordenEnBase(ORDEN_DE_LA_CLIENTA).estado).toBe('CONFIRMADO')
  })

  it('«No, volver» cierra sin cancelar y devuelve el foco al botón', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    ponerOrdenEn(ORDEN_DE_LA_CLIENTA, 'CONFIRMADO')
    abrirPedidoCliente(ORDEN_DE_LA_CLIENTA)
    await usuario.click(await botonCancelar())
    await usuario.type(campoMotivo(), 'Me equivoqué')

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'No, volver' }))

    // Entonces
    expect(screen.queryByRole('textbox', { name: '¿Por qué cancelas el pedido?' })).not.toBeInTheDocument()
    await expect.poll(() => document.activeElement).toBe(screen.getByRole('button', { name: 'Cancelar pedido' }))
    expect(ordenEnBase(ORDEN_DE_LA_CLIENTA).estado).toBe('CONFIRMADO')
  })

  it('la tienda solo cancela en Confirmado, y el cliente lee que la tienda canceló', async () => {
    // Dado: La 5 tiene el pedido de la vecina en Confirmado
    const usuario = userEvent.setup({ delay: null })
    await entrarComo(CORREOS_DE_PRUEBA.tienda)
    ponerOrdenEn(ORDEN_DE_LA_VECINA, 'CONFIRMADO')
    const { unmount } = abrirPedidoTienda(ORDEN_DE_LA_VECINA)

    // Cuando
    await usuario.click(await botonCancelar())
    expect(screen.getByText('El cliente recibe el aviso con tu motivo. No se puede deshacer.')).toBeInTheDocument()
    await usuario.type(campoMotivo(), 'Se nos acabó el producto')
    await usuario.click(confirmar())

    // Entonces
    expect(await screen.findByText(/^A las .*, cancelaste el pedido\.$/)).toBeInTheDocument()
    expect(ordenEnBase(ORDEN_DE_LA_VECINA)).toMatchObject({ estado: 'CANCELADO', canceladaPor: 'TIENDA' })

    // Y la vecina lee quién canceló y por qué
    unmount()
    await entrarComo(CORREOS_DE_PRUEBA.vecina)
    abrirPedidoCliente(ORDEN_DE_LA_VECINA)
    expect(await screen.findByText(/^A las .*, Droguería La 5 canceló el pedido\.$/)).toBeInTheDocument()
    expect(screen.getByText('Motivo: «Se nos acabó el producto»')).toBeInTheDocument()
    expect(screen.getByText(/^Cancelado por la tienda · /)).toBeInTheDocument()
  })

  it('en Preparando la tienda ya no ofrece cancelar', async () => {
    await entrarComo(CORREOS_DE_PRUEBA.tienda)
    ponerOrdenEn(ORDEN_DE_LA_VECINA, 'PREPARANDO')
    abrirPedidoTienda(ORDEN_DE_LA_VECINA)
    expect(await screen.findByRole('button', { name: 'Salió a domicilio' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Cancelar pedido' })).not.toBeInTheDocument()
  })
})
