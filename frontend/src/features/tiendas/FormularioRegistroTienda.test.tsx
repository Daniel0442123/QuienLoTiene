import { screen, within } from '@testing-library/react'
import userEvent, { type UserEvent } from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { FormularioRegistroTienda } from './FormularioRegistroTienda'

// jsdom no dibuja mapas: un toque en el mapa se simula con un botón que entrega la ubicación
vi.mock('@/components/MapaUbicacion', () => ({
  default: ({ alCambiar }: { alCambiar: (c: { latitud: number; longitud: number }) => void }) => (
    <button type="button" onClick={() => alCambiar({ latitud: 2.4448, longitud: -76.6147 })}>
      Tocar el mapa
    </button>
  ),
}))

const RUTA_REGISTRO = '/api/v1/tiendas/registro'
const foto = () => new File([new Uint8Array(2000)], 'fachada.jpg', { type: 'image/jpeg' })
const rut = () => new File([new Uint8Array(3000)], 'rut.pdf', { type: 'application/pdf' })

interface Opciones {
  correo?: string
  sinRut?: boolean
  sinUbicacion?: boolean
}

async function llenar(usuario: UserEvent, { correo = 'dueno@correo.com', sinRut, sinUbicacion }: Opciones = {}) {
  await usuario.type(screen.getByRole('textbox', { name: 'Tu nombre' }), 'Carlos Gómez')
  await usuario.type(screen.getByRole('textbox', { name: 'Correo' }), correo)
  await usuario.type(screen.getByRole('textbox', { name: 'Celular' }), '310 555 1234')
  await usuario.type(screen.getByLabelText('Frase de contraseña'), 'la ferreteria abre temprano')
  await usuario.type(screen.getByRole('textbox', { name: 'Nombre de la tienda' }), 'Droguería Nueva')
  await usuario.click(await screen.findByRole('radio', { name: 'Farmacia' }))
  if (!sinUbicacion) await usuario.click(await screen.findByRole('button', { name: 'Tocar el mapa' }))
  await usuario.upload(screen.getByLabelText('Foto del local'), foto())
  if (!sinRut) await usuario.upload(screen.getByLabelText('RUT o documento del negocio'), rut())
  await usuario.click(screen.getByRole('checkbox', { name: /Autorizo el tratamiento/ }))
}

const botonRegistrar = () => screen.getByRole('button', { name: 'Registrar tienda' })

/** La región de anuncios urgentes también es role="alert": el aviso se busca por su texto. */
async function avisoCon(texto: RegExp) {
  return (await screen.findByText(texto)).closest('[role="alert"]')
}

describe('HU-002 · HU-005 · Registro de tienda', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    servidorSimulado.events.removeAllListeners()
  })

  it('no tiene violaciones de accesibilidad, ni vacío ni con errores', async () => {
    // Dado
    const usuario = userEvent.setup()
    const { container } = renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)
    await screen.findByRole('radio', { name: 'Farmacia' })
    await screen.findByRole('button', { name: 'Tocar el mapa' })
    expect(await violacionesDeAccesibilidad(container)).toEqual([])

    // Cuando
    await usuario.click(botonRegistrar())

    // Entonces
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-TIE-002-01 · registra la tienda con sus documentos y queda PENDIENTE', async () => {
    // Dado
    const usuario = userEvent.setup()
    const alRegistrar = vi.fn()
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={alRegistrar} />)

    // Cuando
    await llenar(usuario)
    await usuario.click(botonRegistrar())

    // Entonces
    await vi.waitFor(() => expect(alRegistrar).toHaveBeenCalledWith('Droguería Nueva'))
    const tienda = base.tiendas.find((t) => t.nombre === 'Droguería Nueva')
    expect(tienda).toMatchObject({ estado: 'PENDIENTE', estaAbierta: false, telefono: '3105551234' })
    expect(tienda?.documentos.map((d) => d.tipo)).toEqual(['FOTO_LOCAL', 'RUT'])
    const dueno = base.usuarios.find((u) => u.correo === 'dueno@correo.com')
    expect(dueno?.rol).toBe('TIENDA')
  })

  it('CP-TIE-002-02 · sin el RUT no envía nada e indica el campo que falta', async () => {
    // Dado
    const usuario = userEvent.setup()
    const peticiones = vi.fn()
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)
    await llenar(usuario, { sinRut: true })
    servidorSimulado.events.on('request:start', ({ request }) => {
      if (request.method === 'POST') peticiones()
    })

    // Cuando
    await usuario.click(botonRegistrar())

    // Entonces
    const campoRut = screen.getByLabelText('RUT o documento del negocio')
    expect(campoRut).toHaveAttribute('aria-invalid', 'true')
    expect(campoRut).toHaveAccessibleDescription(/Adjunta el RUT/)
    expect(campoRut).toHaveFocus()
    const resumen = screen.getByRole('region', { name: /Revisa estos datos/ })
    expect(within(resumen).getByRole('link', { name: /Adjunta el RUT/ })).toBeInTheDocument()
    expect(peticiones).not.toHaveBeenCalled()
  })

  it('CP-TIE-002-02 · si el backend dice que falta un documento (TIE-002) marca ese campo', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(
      http.post(RUTA_REGISTRO, () =>
        HttpResponse.json(
          { title: 'Documento faltante', status: 400, codigo: 'TIE-002', detail: 'Falta el RUT.', errores: [] },
          { status: 400 },
        ),
      ),
    )
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)

    // Cuando
    await llenar(usuario)
    await usuario.click(botonRegistrar())

    // Entonces
    const campoRut = screen.getByLabelText('RUT o documento del negocio')
    await vi.waitFor(() => expect(campoRut).toHaveAttribute('aria-invalid', 'true'))
    expect(campoRut).toHaveFocus()
  })

  it('sin ubicación pide marcarla y lleva el foco a «Usar mi ubicación»', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)

    // Cuando
    await llenar(usuario, { sinUbicacion: true })
    await usuario.click(botonRegistrar())

    // Entonces
    expect(screen.getByRole('button', { name: 'Usar mi ubicación' })).toHaveFocus()
    expect(screen.getByRole('group', { name: 'Ubicación del local' })).toHaveAccessibleDescription(
      /Marca la ubicación del local/,
    )
  })

  it('«Usar mi ubicación» marca la ubicación con el GPS y lo dice en texto', async () => {
    // Dado
    const usuario = userEvent.setup()
    vi.stubGlobal('navigator', {
      ...navigator,
      geolocation: {
        getCurrentPosition: (exito: PositionCallback) =>
          exito({ coords: { latitude: 2.44, longitude: -76.6 } } as GeolocationPosition),
      },
    })
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'Usar mi ubicación' }))

    // Entonces
    expect(screen.getByText('Ubicación marcada en el mapa.')).toBeInTheDocument()
  })

  it('si el permiso de ubicación está bloqueado explica cómo marcarla en el mapa', async () => {
    // Dado
    const usuario = userEvent.setup()
    vi.stubGlobal('navigator', {
      ...navigator,
      geolocation: {
        getCurrentPosition: (_exito: PositionCallback, falla: PositionErrorCallback) =>
          falla({ code: 1, PERMISSION_DENIED: 1 } as GeolocationPositionError),
      },
    })
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'Usar mi ubicación' }))

    // Entonces
    expect(screen.getByText(/el permiso está bloqueado/)).toBeInTheDocument()
    expect(screen.getByText('Todavía no has marcado la ubicación.')).toBeInTheDocument()
  })

  it('TIE-003 · rechaza al momento un archivo que no es foto ni PDF', async () => {
    // Dado
    const usuario = userEvent.setup({ applyAccept: false })
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)
    const campo = screen.getByLabelText('RUT o documento del negocio')

    // Cuando
    await usuario.upload(campo, new File(['x'], 'rut.docx', { type: 'application/msword' }))

    // Entonces
    expect(campo).toHaveAttribute('aria-invalid', 'true')
    expect(campo).toHaveAccessibleDescription(/Ese archivo no sirve/)
  })

  it('GEN-010 · rechaza al momento un PDF de más de 5 MB', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)
    const campo = screen.getByLabelText('RUT o documento del negocio')

    // Cuando
    await usuario.upload(campo, new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'rut.pdf', { type: 'application/pdf' }))

    // Entonces
    await vi.waitFor(() => expect(campo).toHaveAccessibleDescription(/pesa más de 5 MB/))
  })

  it('muestra el archivo elegido con su tamaño', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)
    const campo = screen.getByLabelText('RUT o documento del negocio')

    // Cuando
    await usuario.upload(campo, rut())

    // Entonces
    await vi.waitFor(() => expect(campo).toHaveAccessibleDescription(/rut\.pdf · 3 KB/))
  })

  it('IDE-001 · con un correo que ya tiene cuenta lo marca y ofrece entrar', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)

    // Cuando
    await llenar(usuario, { correo: CORREOS_DE_PRUEBA.tienda })
    await usuario.click(botonRegistrar())

    // Entonces
    const aviso = await avisoCon(/Ya existe una cuenta con ese correo/)
    expect(within(aviso as HTMLElement).getByRole('link', { name: 'Entrar con ese correo' })).toBeInTheDocument()
    expect(screen.getByRole('textbox', { name: 'Correo' })).toHaveFocus()
  })

  it('categorías · estado cargando con esqueleto', async () => {
    // Dado
    servidorSimulado.use(http.get('/api/v1/categorias', () => delay('infinite')))

    // Cuando
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)

    // Entonces
    expect(await screen.findByRole('status', { name: 'Cargando categorías…' })).toBeInTheDocument()
  })

  it('categorías · estado error con reintentar', async () => {
    // Dado
    const usuario = userEvent.setup()
    let intentos = 0
    servidorSimulado.use(
      http.get('/api/v1/categorias', () => {
        intentos += 1
        return intentos === 1
          ? HttpResponse.json({ title: 'Error', status: 500, codigo: 'GEN-008', errores: [] }, { status: 500 })
          : HttpResponse.json([{ id: 'c-1', nombre: 'Farmacia' }])
      }),
    )
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Reintentar' }))

    // Entonces
    expect(await screen.findByRole('radio', { name: 'Farmacia' })).toBeInTheDocument()
  })

  it('categorías · estado vacío dice qué pasa y qué hacer', async () => {
    // Dado
    servidorSimulado.use(http.get('/api/v1/categorias', () => HttpResponse.json([])))

    // Cuando
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)

    // Entonces
    expect(await screen.findByText('Todavía no hay categorías disponibles.')).toBeInTheDocument()
    expect(screen.getByText(/Inténtalo más tarde/)).toBeInTheDocument()
  })

  it('estado enviando · el botón dice que está registrando y no permite otro envío', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(http.post(RUTA_REGISTRO, () => delay('infinite')))
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)

    // Cuando
    await llenar(usuario)
    await usuario.click(botonRegistrar())

    // Entonces
    expect(await screen.findByRole('button', { name: 'Registrando tienda…' })).toBeDisabled()
  })

  it('estado error · un fallo del servidor muestra qué pasó y el código para soporte', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(
      http.post(RUTA_REGISTRO, () =>
        HttpResponse.json(
          { title: 'Error interno', status: 500, codigo: 'GEN-008', idCorrelacion: 'abc-123', errores: [] },
          { status: 500 },
        ),
      ),
    )
    renderizarConProveedores(<FormularioRegistroTienda alRegistrar={vi.fn()} />)

    // Cuando
    await llenar(usuario)
    await usuario.click(botonRegistrar())

    // Entonces
    const aviso = await avisoCon(/Algo falló de nuestro lado/)
    expect(aviso).toHaveTextContent('Código para soporte: abc-123')
  })
})
