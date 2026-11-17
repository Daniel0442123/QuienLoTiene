import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA, TIENDAS_SPRINT_2 } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { MisRespuestasTiendaPagina } from './MisRespuestasTiendaPagina'
import { SolicitudesTiendaPagina } from './SolicitudesTiendaPagina'

const SUERO = 'a5b6c7d8-0002-4e2f-9a3b-1c2d3e4f5a02'
const respuestasDeLa5 = () => base.respuestas.filter((r) => r.tiendaId === TIENDAS_SPRINT_2.la5)
const tarjetas = () => screen.getAllByRole('article')

describe('HU-018 · Solicitudes nuevas de la tienda', () => {
  beforeEach(async () => {
    limpiarSesion()
    // Dado: la tienda de prueba, aprobada y abierta, con dos solicitudes de la vecina sin responder
    await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA)
  })

  it('no tiene violaciones de accesibilidad detectables', async () => {
    const { container } = renderizarConProveedores(<SolicitudesTiendaPagina />, { ruta: '/tienda/solicitudes' })
    await screen.findByText('2 esperando respuesta')
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-SOL-018-02 · pone la urgente primero, dice «Urgente» en texto y la distancia aproximada', async () => {
    // Cuando
    renderizarConProveedores(<SolicitudesTiendaPagina />, { ruta: '/tienda/solicitudes' })

    // Entonces
    await screen.findByText('2 esperando respuesta')
    const [primera, segunda] = tarjetas()
    expect(within(primera!).getByRole('heading', { name: 'Paracetamol 500 mg' })).toBeInTheDocument()
    expect(primera).toHaveTextContent('Urgente')
    expect(primera).toHaveTextContent(/2 unidades · a 300 m · hace 3 min/)
    expect(primera).toHaveTextContent('Pide domicilio')
    expect(primera).toHaveTextContent(/Vence en 2\d min/)
    expect(segunda).not.toHaveTextContent('Urgente')
    // Cada botón repetido dice de qué producto es (WCAG 2.4.6)
    expect(within(primera!).getByRole('link', { name: 'Sí lo tengo: Paracetamol 500 mg' })).toHaveAttribute(
      'href',
      '/tienda/solicitudes/a5b6c7d8-0001-4e2f-9a3b-1c2d3e4f5a01',
    )
    expect(within(primera!).getByRole('link', { name: 'Tengo algo parecido: Paracetamol 500 mg' })).toHaveAttribute(
      'href',
      '/tienda/solicitudes/a5b6c7d8-0001-4e2f-9a3b-1c2d3e4f5a01?tipo=PARECIDO',
    )
  })

  it('CP-SOL-019-03 · «No lo tengo» la quita en un toque y al cerrar el aviso envía NO', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    renderizarConProveedores(<SolicitudesTiendaPagina />, { ruta: '/tienda/solicitudes' })

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'No lo tengo: Suero oral sabor a naranja' }))

    // Entonces: sale de la lista al instante, el foco va a «Deshacer» y todavía no se envió nada
    expect(screen.getByText('1 esperando respuesta')).toBeInTheDocument()
    expect(screen.getByText('Descartaste «Suero oral sabor a naranja».')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Deshacer' })).toHaveFocus()
    expect(respuestasDeLa5().some((r) => r.solicitudId === SUERO)).toBe(false)

    await usuario.click(screen.getByRole('button', { name: 'Cerrar aviso' }))
    await vi.waitFor(() => expect(respuestasDeLa5().find((r) => r.solicitudId === SUERO)).toMatchObject({ tipo: 'NO' }))
    expect(screen.queryByRole('button', { name: 'Deshacer' })).not.toBeInTheDocument()
    expect(screen.queryByText('Suero oral sabor a naranja')).not.toBeInTheDocument()
  })

  it('«Deshacer» la devuelve a la lista con el foco en ella y no envía nada', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    renderizarConProveedores(<SolicitudesTiendaPagina />, { ruta: '/tienda/solicitudes' })
    await usuario.click(await screen.findByRole('button', { name: 'No lo tengo: Suero oral sabor a naranja' }))

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'Deshacer' }))

    // Entonces
    expect(screen.getByText('2 esperando respuesta')).toBeInTheDocument()
    expect(screen.getByRole('article', { name: 'Suero oral sabor a naranja' })).toHaveFocus()
    expect(respuestasDeLa5().some((r) => r.solicitudId === SUERO)).toBe(false)
  })

  it('si no se puede enviar «No lo tengo», la solicitud vuelve y lo avisa', async () => {
    // Dado
    servidorSimulado.use(http.post('/api/v1/solicitudes/:id/respuestas', () => HttpResponse.error()))
    const usuario = userEvent.setup({ delay: null })
    renderizarConProveedores(<SolicitudesTiendaPagina />, { ruta: '/tienda/solicitudes' })
    await usuario.click(await screen.findByRole('button', { name: 'No lo tengo: Suero oral sabor a naranja' }))

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'Cerrar aviso' }))

    // Entonces
    expect(await screen.findByRole('article', { name: 'Suero oral sabor a naranja' })).toBeInTheDocument()
  })

  it('sin solicitudes muestra el estado vacío', async () => {
    // Dado
    servidorSimulado.use(http.get('/api/v1/solicitudes/disponibles', () => HttpResponse.json([])))

    // Cuando
    renderizarConProveedores(<SolicitudesTiendaPagina />, { ruta: '/tienda/solicitudes' })

    // Entonces
    expect(await screen.findByText('No hay solicitudes nuevas por ahora.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Nuevas' })).toHaveAttribute('aria-current', 'page')
  })
})

describe('HU-021 · «Mis respuestas» de la tienda', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA)
  })

  it('dice el estado de cada respuesta en texto, no solo con color', async () => {
    // Cuando
    const { container } = renderizarConProveedores(<MisRespuestasTiendaPagina />, {
      ruta: '/tienda/solicitudes/mis-respuestas',
    })

    // Entonces
    const loratadina = await screen.findByRole('heading', { name: 'Loratadina 10 mg' })
    const tarjeta = loratadina.closest('article')!
    expect(tarjeta).toHaveTextContent('Sí lo tengo · $ 1.500 · 1 unidad')
    expect(tarjeta).toHaveTextContent('Elegida')
    expect(tarjeta).toHaveTextContent('El cliente te eligió. Prepara el pedido.')
    expect(screen.getByRole('link', { name: 'Mis respuestas' })).toHaveAttribute('aria-current', 'page')
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })
})
