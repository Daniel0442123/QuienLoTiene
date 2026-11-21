import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA, TIENDAS_SPRINT_2 } from '@/mocks/datos'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { crearSolicitud, MINUTO } from '@/pruebas/solicitudes'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { HistorialSolicitudesPagina } from './HistorialSolicitudesPagina'
import { MisPedidosPagina } from './MisPedidosPagina'
import { PublicarSolicitudPagina } from './PublicarSolicitudPagina'

vi.mock('@/components/MapaUbicacion', () => ({ default: () => null }))

const AQUI = { latitud: 2.4521, longitud: -76.5987 }

function abrir(ruta: string) {
  return renderizarConProveedores(
    <Routes>
      <Route path="/pedidos" element={<MisPedidosPagina />} />
      <Route path="/solicitudes" element={<HistorialSolicitudesPagina />} />
      <Route path="/solicitudes/nueva" element={<PublicarSolicitudPagina />} />
    </Routes>,
    { ruta },
  )
}

const clienta = () => base.usuarios.find((u) => u.correo === CORREOS_DE_PRUEBA.cliente)!
const deLaClienta = () => base.solicitudes.filter((s) => s.clienteId === clienta().id)
const tarjetas = () => screen.findAllByRole('article')

/** El GPS del celular: responde `AQUI` o falla con el permiso negado. */
function gps(resultado: 'aqui' | 'negado') {
  Object.defineProperty(navigator, 'geolocation', {
    configurable: true,
    value: {
      getCurrentPosition: (exito: PositionCallback, falla: PositionErrorCallback) =>
        resultado === 'aqui'
          ? exito({ coords: { latitude: AQUI.latitud, longitude: AQUI.longitud } } as GeolocationPosition)
          : falla({ code: 1, PERMISSION_DENIED: 1 } as GeolocationPositionError),
    },
  })
}

describe('HU-008 · Historial, repetir y «Mis preguntas» · CP-CLI-008-01, CP-CLI-008-02, CP-CLI-008-03', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })
  afterEach(() => Reflect.deleteProperty(navigator, 'geolocation'))

  it('CP-CLI-008-02 · sin preguntas, invita a hacer la primera', async () => {
    // Dado: la clienta nunca preguntó
    base.solicitudes = base.solicitudes.filter((s) => s.clienteId !== clienta().id)

    // Cuando
    const { container } = abrir('/solicitudes')

    // Entonces
    expect(await screen.findByText('Todavía no has hecho ninguna pregunta.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Hacer mi primera pregunta' })).toHaveAttribute(
      'href',
      '/solicitudes/nueva',
    )
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('muestra todas, de la más reciente a la más antigua, con su estado en texto', async () => {
    // Dado: una abierta recién publicada y otra vencida de ayer
    crearSolicitud({ producto: 'Loratadina 10 mg' })
    crearSolicitud(
      {
        producto: 'Suero oral',
        fechaPublicacion: new Date(Date.now() - 24 * 60 * MINUTO).toISOString(),
        fechaVencimiento: new Date(Date.now() - 23 * 60 * MINUTO).toISOString(),
      },
      false,
    )

    // Cuando
    const { container } = abrir('/solicitudes')

    // Entonces
    const [primera, ...resto] = await tarjetas()
    expect(within(primera!).getByRole('heading', { name: 'Loratadina 10 mg' })).toBeInTheDocument()
    expect(within(primera!).getByText('Abierta')).toBeInTheDocument()
    expect(within(primera!).getByRole('link', { name: 'Ver respuestas de Loratadina 10 mg' })).toBeInTheDocument()
    const suero = resto.find((t) => within(t).queryByRole('heading', { name: 'Suero oral' }))!
    expect(within(suero).getByText('Vencida')).toBeInTheDocument()
    expect(within(suero).getByText('Sin respuestas todavía')).toBeInTheDocument()
    // La que terminó en pedido (Ibuprofeno, de los datos de prueba) lleva al pedido
    const conPedido = resto.find((t) => within(t).queryByText('Elegiste una tienda'))!
    expect(within(conPedido).getByText('Resuelta')).toBeInTheDocument()
    expect(within(conPedido).getByRole('link', { name: 'Ver el pedido' })).toBeInTheDocument()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-CLI-008-01 · «Repetir» publica otra igual con la ubicación de ahora y abre sus respuestas', async () => {
    // Dado: la clienta preguntó por «Paracetamol 500 mg, 1 caja» desde otro lugar
    const usuario = userEvent.setup({ delay: null })
    const originalId = crearSolicitud({ producto: 'Paracetamol 500 mg', cantidad: 1, esUrgente: true }, false)
    const original = base.solicitudes.find((s) => s.id === originalId)!
    gps('aqui')
    abrir('/solicitudes')

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Repetir Paracetamol 500 mg' }))

    // Entonces: queda una nueva con lo mismo, menos la ubicación, y se ven sus respuestas
    expect(await screen.findByRole('heading', { level: 1, name: 'Esperando respuestas' })).toBeInTheDocument()
    expect(await screen.findByText('Publicamos la pregunta de nuevo desde donde estás.')).toBeInTheDocument()
    const nueva = deLaClienta().at(-1)!
    expect(nueva.id).not.toBe(originalId)
    expect(nueva).toMatchObject({
      producto: 'Paracetamol 500 mg',
      cantidad: 1,
      categoriaId: original.categoriaId,
      modalidad: original.modalidad,
      esUrgente: true,
      estado: 'ABIERTA',
      ubicacion: AQUI,
    })
  })

  it('sin permiso de ubicación, repite desde la primera dirección guardada y lo dice', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    crearSolicitud({ producto: 'Curitas' }, false)
    gps('negado')
    abrir('/solicitudes')

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Repetir Curitas' }))

    // Entonces
    expect(
      await screen.findByText('No pudimos usar tu ubicación, así que publicamos la pregunta desde «Casa».'),
    ).toBeInTheDocument()
    const casa = base.direcciones.find((d) => d.clienteId === clienta().id)!
    expect(deLaClienta().at(-1)?.ubicacion).toEqual({ latitud: casa.latitud, longitud: casa.longitud })
  })

  it('repetir cuenta para el límite de 10 por hora (SOL-001)', async () => {
    // Dado: ya publicó 10 en la última hora
    const usuario = userEvent.setup({ delay: null })
    for (let i = 0; i < 10; i += 1) crearSolicitud({ producto: `Producto ${i}` }, false)
    gps('aqui')
    abrir('/solicitudes')
    const antes = deLaClienta().length

    // Cuando
    await usuario.click((await screen.findAllByRole('button', { name: /^Repetir / }))[0]!)

    // Entonces
    expect((await screen.findByText(/10 solicitudes/)).closest('[role="alert"]')).not.toBeNull()
    expect(deLaClienta()).toHaveLength(antes)
  })

  it('pagina de 20 en 20 con «Ver más preguntas» y anuncia cuántas llegaron', async () => {
    // Dado: 25 preguntas en total
    const usuario = userEvent.setup({ delay: null })
    base.solicitudes = base.solicitudes.filter((s) => s.clienteId !== clienta().id)
    for (let i = 0; i < 25; i += 1) {
      crearSolicitud(
        {
          producto: `Producto ${String(i).padStart(2, '0')}`,
          fechaPublicacion: new Date(Date.now() - (i + 1) * 2 * 60 * MINUTO).toISOString(),
          fechaVencimiento: new Date(Date.now() - (i + 1) * 60 * MINUTO).toISOString(),
        },
        false,
      )
    }
    abrir('/solicitudes')
    expect(await tarjetas()).toHaveLength(20)

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'Ver más preguntas' }))

    // Entonces
    expect(await screen.findByText('Se cargaron 5 preguntas más.')).toBeInTheDocument()
    expect(await tarjetas()).toHaveLength(25)
    expect(screen.queryByRole('button', { name: 'Ver más preguntas' })).not.toBeInTheDocument()
  })

  it('SC-018 · «Mis pedidos» muestra las preguntas abiertas para volver a sus respuestas', async () => {
    // Dado: una abierta con respuestas y una vencida
    const abierta = crearSolicitud({ producto: 'Acetaminofén 500 mg' })
    crearSolicitud({ producto: 'Vencida vieja', fechaVencimiento: new Date(Date.now() - MINUTO).toISOString() }, false)

    // Cuando
    abrir('/pedidos')

    // Entonces
    const seccion = await screen.findByRole('region', { name: 'Mis preguntas' })
    const enlace = within(seccion).getByRole('link', { name: /Acetaminofén 500 mg · 1 unidad/ })
    expect(enlace).toHaveAttribute('href', `/solicitudes/nueva?solicitud=${abierta}`)
    expect(enlace).toHaveTextContent(/\d respuestas? · Vence en \d+ min/)
    expect(within(seccion).queryByText(/Vencida vieja/)).not.toBeInTheDocument()
    expect(within(seccion).getByRole('link', { name: 'Ver todas mis preguntas' })).toHaveAttribute(
      'href',
      '/solicitudes',
    )
  })

  it('CP-CLI-008-03 · vuelve a una pregunta abierta desde «Mis pedidos» y ve sus 3 respuestas', async () => {
    // Dado: publicó «Paracetamol 500 mg» hace 10 minutos, tiene 3 respuestas y salió de la pantalla sin elegir
    const usuario = userEvent.setup({ delay: null })
    const id = crearSolicitud({
      producto: 'Paracetamol 500 mg',
      fechaPublicacion: new Date(Date.now() - 10 * MINUTO).toISOString(),
      fechaVencimiento: new Date(Date.now() + 20 * MINUTO).toISOString(),
    })
    // A las dos tiendas simuladas que ya respondieron se suma Droguería La 5
    base.respuestas.push({
      id: crypto.randomUUID(),
      solicitudId: id,
      tiendaId: TIENDAS_SPRINT_2.la5,
      tipo: 'SI',
      precioEnPesos: 900,
      cantidadDisponible: 1,
      descripcionAlternativa: null,
      tiempoEstimadoEnMinutos: null,
      ofreceDomicilio: false,
      costoDomicilioEnPesos: null,
      fechaRespuesta: new Date(Date.now() - 5 * MINUTO).toISOString(),
    })

    // Cuando: abre «Mis pedidos», sección «Mis preguntas», y toca la pregunta
    abrir('/pedidos')
    const seccion = await screen.findByRole('region', { name: 'Mis preguntas' })
    const pregunta = within(seccion).getByRole('link', { name: /Paracetamol 500 mg · 1 unidad/ })
    expect(pregunta).toHaveTextContent(/3 respuestas · Vence en (19|20) min/)
    await usuario.click(pregunta)

    // Entonces: vuelve a la pantalla de respuestas de esa pregunta, con las 3
    expect(await screen.findByRole('heading', { level: 1, name: 'Esperando respuestas' })).toBeInTheDocument()
    expect(await screen.findByText('Droguería La 5')).toBeInTheDocument()
    expect(screen.getByText('Botica San José')).toBeInTheDocument()
    expect(screen.getByText('Farmacia Popular')).toBeInTheDocument()
  })
})
