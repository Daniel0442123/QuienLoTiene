import { screen, within } from '@testing-library/react'
import userEvent, { type UserEvent } from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { MiCuentaPagina } from './MiCuentaPagina'

// jsdom no dibuja mapas: un toque en el mapa se simula con un botón que entrega la ubicación
vi.mock('@/components/MapaUbicacion', () => ({
  default: ({ alCambiar }: { alCambiar: (c: { latitud: number; longitud: number }) => void }) => (
    <button type="button" onClick={() => alCambiar({ latitud: 2.45, longitud: -76.62 })}>
      Tocar el mapa
    </button>
  ),
}))

const cliente = () => base.usuarios.find((u) => u.correo === CORREOS_DE_PRUEBA.cliente)
const direccionesDelCliente = () => base.direcciones.filter((d) => d.clienteId === cliente()?.id)
const seccionDirecciones = () => screen.getByRole('region', { name: 'Mis direcciones' })

async function abrirNuevaDireccion(usuario: UserEvent) {
  await usuario.click(await screen.findByRole('button', { name: 'Agregar dirección' }))
  return screen.getByRole('heading', { name: 'Nueva dirección' }).closest('form') as HTMLFormElement
}

describe('HU-007 · Perfil y direcciones del cliente', () => {
  beforeEach(async () => {
    limpiarSesion()
    // Dado: el cliente con la dirección «Casa» inició sesión
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })
  afterEach(() => servidorSimulado.events.removeAllListeners())

  it('no tiene violaciones de accesibilidad, ni al ver ni al agregar una dirección', async () => {
    const usuario = userEvent.setup()
    const { container } = renderizarConProveedores(<MiCuentaPagina />)
    await screen.findByRole('heading', { name: 'Casa' })
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
    await abrirNuevaDireccion(usuario)
    await screen.findByRole('button', { name: 'Tocar el mapa' })
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('muestra tus datos con el correo solo para leer y tus direcciones', async () => {
    // Cuando
    renderizarConProveedores(<MiCuentaPagina />)

    // Entonces
    expect(await screen.findByRole('textbox', { name: 'Nombre' })).toHaveValue('Clienta de Prueba')
    expect(screen.getByRole('textbox', { name: 'Correo' })).toHaveAttribute('readonly')
    const casa = await screen.findByRole('heading', { name: 'Casa' })
    expect(casa.parentElement).toHaveTextContent('Calle 5 # 10-20, Centro')
    expect(casa.parentElement).toHaveTextContent('Indicaciones: Puerta verde')
  })

  it('CU-03 pasos 14 a 16 · guarda el nombre y el celular nuevos', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<MiCuentaPagina />)
    const celular = await screen.findByRole('textbox', { name: 'Celular' })

    // Cuando
    await usuario.clear(celular)
    await usuario.type(celular, '315 999 8877')
    await usuario.click(screen.getByRole('button', { name: 'Guardar mis datos' }))

    // Entonces
    expect(await screen.findByText('Tus datos quedaron guardados.', { selector: 'p.text-exito' })).toBeInTheDocument()
    expect(cliente()?.telefono).toBe('3159998877')
  })

  it('no guarda un celular inválido y explica el formato', async () => {
    const usuario = userEvent.setup()
    renderizarConProveedores(<MiCuentaPagina />)
    const celular = await screen.findByRole('textbox', { name: 'Celular' })
    await usuario.clear(celular)
    await usuario.type(celular, '12')
    await usuario.click(screen.getByRole('button', { name: 'Guardar mis datos' }))
    expect(celular).toHaveAccessibleDescription(/7 a 15 dígitos/)
    expect(cliente()?.telefono).toBe('3101112233')
  })

  it('CP-CLI-007-01 · guarda «Trabajo» con pin, dirección, indicaciones y teléfono, y queda en la lista', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<MiCuentaPagina />)
    const formulario = await abrirNuevaDireccion(usuario)
    expect(screen.getByRole('heading', { name: 'Nueva dirección' })).toHaveFocus()

    // Cuando
    const en = within(formulario)
    await usuario.type(en.getByRole('textbox', { name: 'Nombre de la dirección' }), 'Trabajo')
    await usuario.click(await en.findByRole('button', { name: 'Tocar el mapa' }))
    await usuario.type(en.getByRole('textbox', { name: 'Dirección' }), 'Carrera 9 # 4-12')
    await usuario.type(en.getByRole('textbox', { name: 'Indicaciones (opcional)' }), 'Segundo piso')
    expect(en.getByRole('textbox', { name: 'Celular de contacto' })).toHaveValue('3101112233')
    await usuario.click(en.getByRole('button', { name: 'Guardar dirección' }))

    // Entonces
    expect(await screen.findByText('Guardaste «Trabajo».')).toBeInTheDocument()
    expect(await within(seccionDirecciones()).findByRole('heading', { name: 'Trabajo' })).toBeInTheDocument()
    expect(direccionesDelCliente().find((d) => d.nombre === 'Trabajo')).toMatchObject({
      latitud: 2.45,
      longitud: -76.62,
      direccionEscrita: 'Carrera 9 # 4-12',
      indicaciones: 'Segundo piso',
    })
    expect(screen.getByRole('heading', { name: 'Mis direcciones' })).toHaveFocus()
  })

  it('CP-CLI-007-02 · sin ubicación en el mapa no envía nada e indica el dato faltante', async () => {
    // Dado
    const usuario = userEvent.setup()
    const peticiones = vi.fn()
    servidorSimulado.events.on('request:start', ({ request }) => {
      if (request.method === 'POST') peticiones()
    })
    renderizarConProveedores(<MiCuentaPagina />)
    const formulario = await abrirNuevaDireccion(usuario)
    const en = within(formulario)

    // Cuando
    await usuario.type(en.getByRole('textbox', { name: 'Nombre de la dirección' }), 'Trabajo')
    await usuario.type(en.getByRole('textbox', { name: 'Dirección' }), 'Carrera 9 # 4-12')
    await usuario.click(en.getByRole('button', { name: 'Guardar dirección' }))

    // Entonces
    expect(en.getByRole('group', { name: 'Ubicación en el mapa' })).toHaveAccessibleDescription(/Marca la ubicación/)
    expect(en.getByRole('button', { name: 'Usar mi ubicación' })).toHaveFocus()
    expect(en.getByRole('link', { name: /Marca la ubicación/ })).toBeInTheDocument()
    expect(peticiones).not.toHaveBeenCalled()
  })

  it('edita el nombre de una dirección', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<MiCuentaPagina />)

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Editar Casa' }))
    const nombre = screen.getByRole('textbox', { name: 'Nombre de la dirección' })
    await usuario.clear(nombre)
    await usuario.type(nombre, 'Casa de mamá')
    await usuario.click(screen.getByRole('button', { name: 'Guardar dirección' }))

    // Entonces
    expect(await screen.findByRole('heading', { name: 'Casa de mamá' })).toBeInTheDocument()
    expect(direccionesDelCliente()[0]?.nombre).toBe('Casa de mamá')
  })

  it('borrar pide confirmar en la tarjeta y deja la lista vacía con qué hacer', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<MiCuentaPagina />)

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Borrar Casa' }))
    const confirmar = screen.getByRole('button', { name: 'Sí, borrar' })
    expect(confirmar).toHaveFocus()
    expect(direccionesDelCliente()).toHaveLength(1)
    await usuario.click(confirmar)

    // Entonces
    expect(await screen.findByText('Todavía no tienes direcciones guardadas.')).toBeInTheDocument()
    expect(screen.getByText(/Agrega la de tu casa o tu trabajo/)).toBeInTheDocument()
    expect(direccionesDelCliente()).toHaveLength(0)
  })

  it('estado deshabilitado · con 10 direcciones no deja agregar otra y dice por qué', async () => {
    // Dado
    const [casa] = direccionesDelCliente()
    for (let i = 1; i < 10; i++) base.direcciones.push({ ...casa!, id: `d-${i}`, nombre: `Dirección ${i}` })

    // Cuando
    renderizarConProveedores(<MiCuentaPagina />)

    // Entonces
    const agregar = await screen.findByRole('button', { name: 'Agregar dirección' })
    expect(agregar).toBeDisabled()
    expect(agregar).toHaveAccessibleDescription(/Ya tienes 10 direcciones/)
    expect(screen.getByText('10 de 10 direcciones.')).toBeInTheDocument()
  })

  it('CLI-002 · si el backend responde que ya hay 10, lo explica en el formulario', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(
      http.post('/api/v1/clientes/me/direcciones', () =>
        HttpResponse.json(
          { title: 'Límite de direcciones', status: 422, codigo: 'CLI-002', errores: [] },
          { status: 422 },
        ),
      ),
    )
    renderizarConProveedores(<MiCuentaPagina />)
    const formulario = await abrirNuevaDireccion(usuario)
    const en = within(formulario)

    // Cuando
    await usuario.type(en.getByRole('textbox', { name: 'Nombre de la dirección' }), 'Trabajo')
    await usuario.click(await en.findByRole('button', { name: 'Tocar el mapa' }))
    await usuario.type(en.getByRole('textbox', { name: 'Dirección' }), 'Carrera 9 # 4-12')
    await usuario.click(en.getByRole('button', { name: 'Guardar dirección' }))

    // Entonces
    expect(await en.findByText(/Borra una para agregar otra/)).toBeInTheDocument()
  })

  it('estado cargando con esqueleto', async () => {
    servidorSimulado.use(http.get('/api/v1/usuarios/me', () => delay('infinite')))
    renderizarConProveedores(<MiCuentaPagina />)
    expect(await screen.findByRole('status', { name: 'Cargando tu cuenta…' })).toBeInTheDocument()
  })

  it('estado error · las direcciones que no cargan se pueden reintentar', async () => {
    // Dado
    const usuario = userEvent.setup()
    let intentos = 0
    servidorSimulado.use(
      http.get('/api/v1/clientes/me/direcciones', () => {
        intentos += 1
        if (intentos === 1) {
          return HttpResponse.json({ title: 'Error', status: 500, codigo: 'GEN-008', errores: [] }, { status: 500 })
        }
        return undefined
      }),
    )
    renderizarConProveedores(<MiCuentaPagina />)

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Reintentar' }, { timeout: 5000 }))

    // Entonces
    expect(await screen.findByRole('heading', { name: 'Casa' })).toBeInTheDocument()
  })
})
