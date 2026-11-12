import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { FormularioPublicarSolicitud } from './FormularioPublicarSolicitud'

// jsdom no dibuja mapas: un toque en el mapa se simula con un botón que entrega la ubicación
vi.mock('@/components/MapaUbicacion', () => ({
  default: ({ alCambiar }: { alCambiar: (c: { latitud: number; longitud: number }) => void }) => (
    <button type="button" onClick={() => alCambiar({ latitud: 2.45, longitud: -76.61 })}>
      Tocar el mapa
    </button>
  ),
}))

const RUTA = '/api/v1/solicitudes'
const campoProducto = () => screen.getByRole('textbox', { name: '¿Qué estás buscando?' })
const botonPreguntar = () => screen.getByRole('button', { name: 'Preguntar' })
const sinDirecciones = () =>
  servidorSimulado.use(http.get('/api/v1/clientes/me/direcciones', () => HttpResponse.json([])))

describe('HU-016 · Publicar solicitud', () => {
  beforeEach(async () => {
    limpiarSesion()
    // Dado: un cliente con una dirección guardada («Casa») inició sesión
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })
  afterEach(() => servidorSimulado.events.removeAllListeners())

  it('no tiene violaciones de accesibilidad, ni vacío ni con errores', async () => {
    // Dado
    const usuario = userEvent.setup()
    const { container } = renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={vi.fn()} />)
    await screen.findByRole('radio', { name: 'Farmacia' })
    await screen.findByRole('radio', { name: /Casa/ })
    expect(await violacionesDeAccesibilidad(container)).toEqual([])

    // Cuando
    await usuario.click(botonPreguntar())

    // Entonces
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('RNF-027 · el único campo por escribir recibe el foco y la ubicación ya viene elegida', async () => {
    // Cuando
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={vi.fn()} />)

    // Entonces
    await vi.waitFor(() => expect(campoProducto()).toHaveFocus())
    expect(await screen.findByRole('radio', { name: /Casa/ })).toBeChecked()
    expect(screen.getByRole('radio', { name: 'Lo recojo en la tienda' })).toBeChecked()
    expect(screen.getByRole('textbox', { name: 'Cantidad', hidden: true })).toHaveValue('1')
  })

  it('CP-SOL-016-01 · publica «Paracetamol 500 mg, 1 caja» en farmacias para recoger y queda ABIERTA', async () => {
    // Dado
    const usuario = userEvent.setup()
    const alPublicar = vi.fn()
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={alPublicar} />)
    await screen.findByRole('radio', { name: /Casa/ })

    // Cuando: 1 campo escrito y 2 toques
    await usuario.type(campoProducto(), 'Paracetamol 500 mg, 1 caja')
    await usuario.click(await screen.findByRole('radio', { name: 'Farmacia' }))
    await usuario.click(botonPreguntar())

    // Entonces
    await vi.waitFor(() => expect(alPublicar).toHaveBeenCalled())
    const publicada = alPublicar.mock.calls[0]?.[0]
    expect(publicada).toMatchObject({ estado: 'ABIERTA', producto: 'Paracetamol 500 mg, 1 caja', modalidad: 'RECOGER' })
    const minutos = (Date.parse(publicada.fechaVencimiento) - Date.parse(publicada.fechaPublicacion)) / 60_000
    expect(minutos).toBe(30)
    const casa = base.direcciones[0]
    expect(base.solicitudes[0]).toMatchObject({
      cantidad: 1,
      esUrgente: false,
      ubicacion: { latitud: casa?.latitud, longitud: casa?.longitud },
    })
  })

  it('CP-SOL-016-02 · sin producto no envía nada, lo indica y lleva el foco al campo', async () => {
    // Dado
    const usuario = userEvent.setup()
    const peticiones = vi.fn()
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={vi.fn()} />)
    await usuario.click(await screen.findByRole('radio', { name: 'Farmacia' }))
    servidorSimulado.events.on('request:start', ({ request }) => {
      if (request.method === 'POST') peticiones()
    })

    // Cuando
    await usuario.click(botonPreguntar())

    // Entonces
    expect(campoProducto()).toHaveAttribute('aria-invalid', 'true')
    expect(campoProducto()).toHaveAccessibleDescription(/Escribe qué estás buscando/)
    await vi.waitFor(() => expect(campoProducto()).toHaveFocus())
    expect(peticiones).not.toHaveBeenCalled()
  })

  it('CP-SOL-016-02 · sin ubicación no envía nada e indica el dato faltante', async () => {
    // Dado
    sinDirecciones()
    const usuario = userEvent.setup()
    const peticiones = vi.fn()
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={vi.fn()} />)
    await usuario.type(campoProducto(), 'Tornillos de 2 pulgadas')
    await usuario.click(await screen.findByRole('radio', { name: 'Ferretería' }))
    servidorSimulado.events.on('request:start', ({ request }) => {
      if (request.method === 'POST') peticiones()
    })

    // Cuando
    await usuario.click(botonPreguntar())

    // Entonces
    expect(screen.getByRole('group', { name: '¿Cerca de dónde?' })).toHaveAccessibleDescription(/Elige cerca de dónde/)
    expect(screen.getByRole('link', { name: /Elige cerca de dónde/ })).toBeInTheDocument()
    expect(peticiones).not.toHaveBeenCalled()
  })

  it('sin direcciones guardadas ofrece el GPS y el mapa, y con un toque en el mapa publica', async () => {
    // Dado
    sinDirecciones()
    const usuario = userEvent.setup()
    const alPublicar = vi.fn()
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={alPublicar} />)

    // Cuando
    await usuario.type(campoProducto(), 'Tornillos de 2 pulgadas')
    await usuario.click(await screen.findByRole('radio', { name: 'Ferretería' }))
    await usuario.click(await screen.findByRole('button', { name: 'Tocar el mapa' }))
    await usuario.click(botonPreguntar())

    // Entonces
    expect(screen.getByText(/Si guardas tus direcciones/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Usar mi ubicación' })).toBeInTheDocument()
    await vi.waitFor(() => expect(alPublicar).toHaveBeenCalled())
  })

  it('«Otra ubicación» abre el mapa para elegir otro punto', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={vi.fn()} />)

    // Cuando
    await usuario.click(await screen.findByRole('radio', { name: 'Otra ubicación' }))

    // Entonces
    expect(await screen.findByRole('button', { name: 'Tocar el mapa' })).toBeInTheDocument()
  })

  it('CP-SOL-016-03 · con el límite de 10 por hora dice cuándo puede volver a intentarlo', async () => {
    // Dado: el cliente ya publicó 10 solicitudes en la última hora
    const usuario = userEvent.setup()
    servidorSimulado.use(
      http.post(RUTA, () =>
        HttpResponse.json(
          { title: 'Límite de solicitudes superado', status: 429, codigo: 'SOL-001', errores: [] },
          { status: 429, headers: { 'Retry-After': '1500' } },
        ),
      ),
    )
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={vi.fn()} />)
    await screen.findByRole('radio', { name: /Casa/ })

    // Cuando
    await usuario.type(campoProducto(), 'Paracetamol')
    await usuario.click(await screen.findByRole('radio', { name: 'Farmacia' }))
    await usuario.click(botonPreguntar())

    // Entonces
    const aviso = (await screen.findByText(/Ya publicaste 10 solicitudes/)).closest('[role="alert"]')
    expect(aviso).toHaveTextContent('Podrás publicar otra en 25 minutos.')
  })

  it('una cantidad inválida abre «Más detalles» y lleva el foco a la cantidad', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={vi.fn()} />)
    await screen.findByRole('radio', { name: /Casa/ })
    await usuario.type(campoProducto(), 'Paracetamol')
    await usuario.click(await screen.findByRole('radio', { name: 'Farmacia' }))
    await usuario.click(screen.getByText('Más detalles (opcional)'))
    const cantidad = screen.getByRole('textbox', { name: 'Cantidad' })
    await usuario.clear(cantidad)
    await usuario.type(cantidad, '0')
    await usuario.click(screen.getByText('Más detalles (opcional)'))

    // Cuando
    await usuario.click(botonPreguntar())

    // Entonces
    await vi.waitFor(() => expect(screen.getByRole('textbox', { name: 'Cantidad' })).toHaveFocus())
    expect(screen.getByRole('textbox', { name: 'Cantidad' })).toHaveAccessibleDescription(/entre 1 y 999/)
  })

  it('direcciones · estado cargando', async () => {
    servidorSimulado.use(http.get('/api/v1/clientes/me/direcciones', () => delay('infinite')))
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={vi.fn()} />)
    expect(await screen.findByRole('status', { name: 'Cargando tus direcciones…' })).toBeInTheDocument()
  })

  it('direcciones · estado error con reintentar y el mapa como alternativa', async () => {
    // Dado
    servidorSimulado.use(
      http.get('/api/v1/clientes/me/direcciones', () =>
        HttpResponse.json({ title: 'Error', status: 500, codigo: 'GEN-008', errores: [] }, { status: 500 }),
      ),
    )

    // Cuando
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={vi.fn()} />)

    // Entonces
    expect(await screen.findByRole('button', { name: 'Reintentar' }, { timeout: 5000 })).toBeInTheDocument()
    expect(screen.getByText(/No pudimos cargar tus direcciones guardadas/)).toBeInTheDocument()
    expect(await screen.findByRole('button', { name: 'Tocar el mapa' })).toBeInTheDocument()
  })

  it('estado enviando · el botón dice que está preguntando y no permite otro envío', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(http.post(RUTA, () => delay('infinite')))
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={vi.fn()} />)
    await screen.findByRole('radio', { name: /Casa/ })

    // Cuando
    await usuario.type(campoProducto(), 'Paracetamol')
    await usuario.click(await screen.findByRole('radio', { name: 'Farmacia' }))
    await usuario.click(botonPreguntar())

    // Entonces
    expect(await screen.findByRole('button', { name: 'Preguntando…' })).toBeDisabled()
  })

  it('estado error · un fallo del servidor muestra qué pasó y el código para soporte', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(
      http.post(RUTA, () =>
        HttpResponse.json(
          { title: 'Error interno', status: 500, codigo: 'GEN-008', idCorrelacion: 'abc-123', errores: [] },
          { status: 500 },
        ),
      ),
    )
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={vi.fn()} />)
    await screen.findByRole('radio', { name: /Casa/ })

    // Cuando
    await usuario.type(campoProducto(), 'Paracetamol')
    await usuario.click(await screen.findByRole('radio', { name: 'Farmacia' }))
    await usuario.click(botonPreguntar())

    // Entonces
    const aviso = (await screen.findByText(/Algo falló de nuestro lado/)).closest('[role="alert"]')
    expect(aviso).toHaveTextContent('Código para soporte: abc-123')
  })
})
