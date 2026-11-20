import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ConexionTiempoReal } from '@/features/notificaciones/ConexionTiempoReal'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { ChatPedidoClientePagina, ChatPedidoTiendaPagina } from '@/pages/ChatPedidoPagina'
import { PedidoPagina } from '@/pages/PedidoPagina'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { ORDEN_DE_LA_VECINA, ponerOrdenEn } from '@/pruebas/ordenes'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'

function abrir(ruta: string) {
  return renderizarConProveedores(
    <>
      <Routes>
        <Route path="/pedidos/:ordenId" element={<PedidoPagina />} />
        <Route path="/pedidos/:ordenId/chat" element={<ChatPedidoClientePagina />} />
        <Route path="/tienda/pedidos/:ordenId/chat" element={<ChatPedidoTiendaPagina />} />
      </Routes>
      <ConexionTiempoReal />
    </>,
    { ruta },
  )
}

/** La otra parte escribe desde su propio celular: su petición va con su token, no con la sesión de la prueba. */
async function escribirComo(correo: string, texto: string) {
  const login = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ correo, fraseContrasena: FRASE_DE_PRUEBA }),
  })
  const { tokenAcceso } = (await login.json()) as { tokenAcceso: string }
  return fetch(`/api/v1/ordenes/${ORDEN_DE_LA_VECINA}/mensajes`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${tokenAcceso}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({ texto }),
  })
}

const mensajes = () => screen.findByRole('log', { name: 'Mensajes del pedido' })
const campo = () => screen.getByRole('textbox', { name: 'Escribe un mensaje' })

async function entrarComo(correo: string) {
  limpiarSesion()
  await iniciarSesion(correo, FRASE_DE_PRUEBA)
}

describe('HU-033 · Chat del pedido · CP-MEN-033-01, CP-MEN-033-02', () => {
  beforeEach(() => entrarComo(CORREOS_DE_PRUEBA.vecina))

  it('no tiene violaciones de accesibilidad y dice en texto quién escribió cada mensaje', async () => {
    // Cuando: la vecina abre el chat de su pedido con La 5 desde el pedido
    const usuario = userEvent.setup({ delay: null })
    const { container } = abrir(`/pedidos/${ORDEN_DE_LA_VECINA}`)
    await usuario.click(await screen.findByRole('link', { name: 'Abrir chat en la app' }))

    // Entonces
    expect(await screen.findByRole('heading', { level: 1, name: 'Chat con Droguería La 5' })).toBeInTheDocument()
    const [mio, deLaTienda] = within(await mensajes()).getAllByRole('listitem')
    expect(mio).toHaveTextContent(/^Tú dijiste:¿Tienen cambio de 50\.000\?a las /)
    expect(deLaTienda).toHaveTextContent(/^Droguería La 5 dijo:Sí, el domiciliario lleva cambio\./)
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-MEN-033-01 · la vecina envía un mensaje y la tienda lo ve sin recargar', async () => {
    // Dado: la tienda tiene el chat abierto
    await entrarComo(CORREOS_DE_PRUEBA.tienda)
    abrir(`/tienda/pedidos/${ORDEN_DE_LA_VECINA}/chat`)
    expect(await screen.findByRole('heading', { level: 1, name: 'Chat con Vecina de Prueba' })).toBeInTheDocument()
    await within(await mensajes()).findByText('Sí, el domiciliario lleva cambio.')

    // Cuando: la vecina escribe desde su celular
    const respuesta = await escribirComo(CORREOS_DE_PRUEBA.vecina, '¿Llegan en 20 minutos?')
    expect(respuesta.status).toBe(201)

    // Entonces: aparece en la región role="log", que lo lee sin mover el foco
    expect(await within(await mensajes()).findByText('¿Llegan en 20 minutos?')).toBeInTheDocument()
  })

  it('envía desde el campo, lo agrega al final y deja el foco para seguir escribiendo', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    abrir(`/pedidos/${ORDEN_DE_LA_VECINA}/chat`)
    await mensajes()

    // Cuando
    await usuario.type(campo(), 'Perfecto, gracias. Es la casa de la reja verde.')
    await usuario.click(screen.getByRole('button', { name: 'Enviar' }))

    // Entonces
    // Se busca el último en cada intento: el mensaje nuevo llega después del clic
    await vi.waitFor(async () =>
      expect((await within(await mensajes()).findAllByRole('listitem')).at(-1)).toHaveTextContent(
        'Perfecto, gracias. Es la casa de la reja verde.',
      ),
    )
    expect(campo()).toHaveValue('')
    await vi.waitFor(() => expect(campo()).toHaveFocus())
    expect(base.mensajes.at(-1)).toMatchObject({
      autor: 'CLIENTE',
      texto: 'Perfecto, gracias. Es la casa de la reja verde.',
    })
  })

  it('no envía un mensaje vacío', async () => {
    const usuario = userEvent.setup({ delay: null })
    abrir(`/pedidos/${ORDEN_DE_LA_VECINA}/chat`)
    await mensajes()
    const antes = base.mensajes.length
    await usuario.type(campo(), '   ')
    await usuario.click(screen.getByRole('button', { name: 'Enviar' }))
    expect(await screen.findByText('Escribe el mensaje antes de enviarlo.')).toBeInTheDocument()
    expect(base.mensajes).toHaveLength(antes)
  })

  it('CP-MEN-033-02 · con el pedido resuelto el chat es de solo lectura', async () => {
    // Dado
    ponerOrdenEn(ORDEN_DE_LA_VECINA, 'RESUELTA')

    // Cuando
    const { container } = abrir(`/pedidos/${ORDEN_DE_LA_VECINA}/chat`)

    // Entonces: se leen los mensajes, pero no hay campo para escribir
    expect(await within(await mensajes()).findByText('¿Tienen cambio de 50.000?')).toBeInTheDocument()
    expect(screen.getByText('El pedido terminó, así que el chat quedó de solo lectura.')).toBeInTheDocument()
    expect(screen.queryByRole('textbox', { name: 'Escribe un mensaje' })).not.toBeInTheDocument()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-MEN-033-02 · si el pedido termina mientras escribe, MEN-002 lo dice y pasa a solo lectura', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    abrir(`/pedidos/${ORDEN_DE_LA_VECINA}/chat`)
    await mensajes()
    await usuario.type(campo(), 'Una pregunta más')
    ponerOrdenEn(ORDEN_DE_LA_VECINA, 'CANCELADO')
    const antes = base.mensajes.length

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'Enviar' }))

    // Entonces
    const aviso = await screen.findByText('El pedido terminó, así que el chat quedó de solo lectura.')
    expect(aviso.closest('[role="alert"]')).not.toBeNull()
    expect(screen.queryByRole('textbox', { name: 'Escribe un mensaje' })).not.toBeInTheDocument()
    expect(base.mensajes).toHaveLength(antes)
  })

  it('un pedido ajeno no muestra su chat (ORD-001)', async () => {
    await entrarComo(CORREOS_DE_PRUEBA.cliente)
    abrir(`/pedidos/${ORDEN_DE_LA_VECINA}/chat`)
    expect(await screen.findByText('No encontramos ese pedido.')).toBeInTheDocument()
    expect(screen.queryByRole('log')).not.toBeInTheDocument()
  })
})
