import { screen, within } from '@testing-library/react'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { CATEGORIAS_SIMULADAS, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { PedidoPagina } from '@/pages/PedidoPagina'
import { SolicitudClientePagina } from '@/pages/SolicitudClientePagina'
import { SolicitudesTiendaPagina } from '@/pages/SolicitudesTiendaPagina'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { crearSolicitud } from '@/pruebas/solicitudes'
import { sonarAlertaUrgente } from '@/services/alertaSonora'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { ConexionTiempoReal } from './ConexionTiempoReal'

vi.mock('@/services/alertaSonora', () => ({ sonarAlertaUrgente: vi.fn() }))

/** Otra persona usa la app al mismo tiempo: sus peticiones van con su propio token, no con la sesión de la prueba. */
async function tokenDe(correo: string): Promise<string> {
  const respuesta = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ correo, fraseContrasena: FRASE_DE_PRUEBA }),
  })
  return ((await respuesta.json()) as { tokenAcceso: string }).tokenAcceso
}

async function comoOtro(correo: string, ruta: string, metodo: string, cuerpo?: unknown) {
  const respuesta = await fetch(ruta, {
    method: metodo,
    headers: { Authorization: `Bearer ${await tokenDe(correo)}`, 'Content-Type': 'application/json' },
    body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
  })
  return respuesta.json() as Promise<{ id: string }>
}

const publicarComoCliente = (producto: string, esUrgente: boolean) =>
  comoOtro(CORREOS_DE_PRUEBA.cliente, '/api/v1/solicitudes', 'POST', {
    producto,
    cantidad: 1,
    categoriaId: CATEGORIAS_SIMULADAS[0].id,
    modalidad: 'RECOGER',
    esUrgente,
    ubicacion: { latitud: 2.4419, longitud: -76.6063 },
  })

/** Espera a que la conexión de tiempo real esté abierta: el primer evento que llega lo demuestra. */
const conectada = () => new Promise((resuelve) => setTimeout(resuelve, 50))

describe('HU-030 · Tiempo real en el panel de la tienda · CP-NOT-030-01, CP-NOT-030-02', () => {
  beforeEach(async () => {
    limpiarSesion()
    vi.mocked(sonarAlertaUrgente).mockClear()
    await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA)
  })

  it('CP-NOT-030-01 · una solicitud nueva aparece sin recargar y se anuncia', async () => {
    // Dado
    renderizarConProveedores(
      <>
        <SolicitudesTiendaPagina />
        <ConexionTiempoReal />
      </>,
    )
    await screen.findByText('2 esperando respuesta')
    await conectada()

    // Cuando
    await publicarComoCliente('Curitas', false)

    // Entonces
    expect(await screen.findByRole('article', { name: 'Curitas' })).toBeInTheDocument()
    expect(screen.getByText('3 esperando respuesta')).toBeInTheDocument()
    expect(await screen.findByText('Solicitud nueva: «Curitas».')).toBeInTheDocument()
    expect(sonarAlertaUrgente).not.toHaveBeenCalled()
  })

  it('CP-NOT-030-02 · la urgente aparece primero, suena la alerta y se anuncia en texto', async () => {
    // Dado
    renderizarConProveedores(
      <>
        <SolicitudesTiendaPagina />
        <ConexionTiempoReal />
      </>,
    )
    await screen.findByText('2 esperando respuesta')
    await conectada()

    // Cuando: una normal y una urgente al mismo tiempo
    await publicarComoCliente('Curitas', false)
    await publicarComoCliente('Salbutamol inhalador', true)

    // Entonces
    await screen.findByRole('article', { name: 'Salbutamol inhalador' })
    await vi.waitFor(() => expect(screen.getAllByRole('article')).toHaveLength(4))
    const urgentes = screen.getAllByRole('article').filter((tarjeta) => within(tarjeta).queryByText('Urgente'))
    expect(screen.getAllByRole('article')[0]).toBe(urgentes[0])
    expect(urgentes.map((tarjeta) => within(tarjeta).getByRole('heading').textContent)).toEqual([
      'Salbutamol inhalador',
      'Paracetamol 500 mg',
    ])
    expect(sonarAlertaUrgente).toHaveBeenCalledOnce()
    expect(
      await screen.findByText('Solicitud urgente: «Salbutamol inhalador». Está primero en tu lista.'),
    ).toBeInTheDocument()
  })
})

describe('HU-030 · Tiempo real para el cliente · CP-SOL-020-01, CP-ORD-027-01', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })

  it('CP-SOL-020-01 · la respuesta de una tienda aparece sin recargar y se anuncia', async () => {
    // Dado: una solicitud sin respuestas, con la pantalla abierta
    const solicitudId = crearSolicitud({ ubicacion: { latitud: 2.4419, longitud: -76.6063 } }, false)
    renderizarConProveedores(
      <>
        <Routes>
          <Route path="/solicitudes/:solicitudId" element={<SolicitudClientePagina />} />
        </Routes>
        <ConexionTiempoReal />
      </>,
      { ruta: `/solicitudes/${solicitudId}` },
    )
    await screen.findByText('Todavía no hay respuestas')
    await conectada()

    // Cuando: Droguería La 5 responde desde su celular
    await comoOtro(CORREOS_DE_PRUEBA.tienda, `/api/v1/solicitudes/${solicitudId}/respuestas`, 'POST', {
      tipo: 'SI',
      precioEnPesos: 8000,
      cantidadDisponible: 1,
    })

    // Entonces
    expect(await screen.findByRole('article', { name: 'Droguería La 5' })).toBeInTheDocument()
    expect(await screen.findByText('Llegó una respuesta de Droguería La 5.')).toBeInTheDocument()
  })

  it('CP-ORD-027-01 · cuando la tienda avanza el pedido, el cliente lo ve sin recargar y se anuncia', async () => {
    // Dado: un pedido CONFIRMADO de la clienta, creado con su propia sesión
    const solicitudId = crearSolicitud()
    const { respuestas } = (await (
      await fetch(`/api/v1/solicitudes/${solicitudId}/respuestas`, {
        headers: { Authorization: `Bearer ${await tokenDe(CORREOS_DE_PRUEBA.cliente)}` },
      })
    ).json()) as { respuestas: Array<{ id: string; nombreTienda: string }> }
    const botica = respuestas.find((r) => r.nombreTienda === 'Botica San José')!
    const orden = await comoOtro(CORREOS_DE_PRUEBA.cliente, '/api/v1/ordenes', 'POST', {
      respuestaId: botica.id,
      modalidad: 'RECOGER',
      medioPago: 'EFECTIVO',
    })
    renderizarConProveedores(
      <>
        <Routes>
          <Route path="/pedidos/:ordenId" element={<PedidoPagina />} />
        </Routes>
        <ConexionTiempoReal />
      </>,
      { ruta: `/pedidos/${orden.id}` },
    )
    expect(await screen.findByText('Confirmado', { selector: 'p' })).toBeInTheDocument()
    await conectada()

    // Cuando: Botica San José empieza a prepararlo
    await comoOtro(CORREOS_DE_PRUEBA.tiendaBotica, `/api/v1/ordenes/${orden.id}/estado`, 'PATCH', {
      estado: 'PREPARANDO',
    })

    // Entonces
    expect(await screen.findByText('Preparando', { selector: 'p' })).toBeInTheDocument()
    expect(await screen.findByText('Tu pedido ahora está: Preparando.')).toBeInTheDocument()
  })
})
