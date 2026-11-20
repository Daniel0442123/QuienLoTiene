import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA, TIENDAS_SPRINT_2 } from '@/mocks/datos'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { crearSolicitud } from '@/pruebas/solicitudes'
import { clienteApi } from '@/services/clienteApi'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { DetalleTiendaPagina } from './DetalleTiendaPagina'
import { SolicitudClientePagina } from './SolicitudClientePagina'

// jsdom no dibuja mapas: se reemplaza por una región con su etiqueta
vi.mock('@/components/MapaUbicacion', () => ({
  default: ({ lugar }: { lugar?: string }) => <div role="region" aria-label={`Mapa con la ubicación de ${lugar}`} />,
}))

function abrir(ruta: string) {
  return renderizarConProveedores(
    <Routes>
      <Route path="/solicitudes/:solicitudId" element={<SolicitudClientePagina />} />
      <Route path="/tiendas/:tiendaId" element={<DetalleTiendaPagina />} />
    </Routes>,
    { ruta },
  )
}

const la5 = () => base.tiendas.find((t) => t.id === TIENDAS_SPRINT_2.la5)!

describe('HU-014 · Detalle de la tienda · CP-TIE-014-01, CP-TIE-014-02', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })

  it('CP-TIE-014-01 · muestra horario, ubicación, teléfono, domicilio, medios de pago y reseñas', async () => {
    // Dado: Droguería La 5 hace domicilio, recibe efectivo y transferencia y tiene 3 reseñas (promedio 4,7)
    Object.assign(la5(), {
      domicilio: { haceDomicilio: true, radioCoberturaEnMetros: 3000, tarifaFijaEnPesos: 4000 },
      mediosPago: ['EFECTIVO', 'TRANSFERENCIA'],
    })

    // Cuando
    const { container } = abrir(`/tiendas/${TIENDAS_SPRINT_2.la5}`)

    // Entonces
    const titulo = await screen.findByRole('heading', { level: 1, name: 'Droguería La 5' })
    await vi.waitFor(() => expect(titulo).toHaveFocus())
    expect(await screen.findByText('Farmacia')).toBeInTheDocument()
    expect(screen.getByText('Abierta ahora')).toBeInTheDocument()
    expect(await screen.findByText('4,7 de 5 · 3 reseñas')).toBeInTheDocument()
    // Horario de lunes a domingo, con los días sin franja como «Cerrado»
    expect(screen.getByText('lunes')).toBeInTheDocument()
    expect(screen.getAllByText(/^8:00\sa\.\s?m\. a 6:00\sp\.\s?m\.$/)).toHaveLength(5)
    expect(screen.getByText(/^9:00\sa\.\s?m\. a 1:00\sp\.\s?m\.$/)).toBeInTheDocument()
    expect(screen.getByText('Cerrado')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: la5().telefono })).toHaveAttribute('href', `tel:${la5().telefono}`)
    expect(await screen.findByRole('region', { name: 'Mapa con la ubicación de Droguería La 5' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /Cómo llegar/ })).toHaveAttribute(
      'href',
      `https://www.google.com/maps/dir/?api=1&destination=${la5().latitud},${la5().longitud}`,
    )
    expect(screen.getByText(/^Hasta 3 km a la redonda · \$\s4\.000$/)).toBeInTheDocument()
    expect(screen.getByText('Efectivo, Transferencia (Nequi, Daviplata, banco)')).toBeInTheDocument()
    // Reseñas sin el nombre del cliente
    expect(screen.getByText('«Tenían el medicamento y llegó rápido.»')).toBeInTheDocument()
    expect(screen.getByText('Sin comentario')).toBeInTheDocument()
    expect(screen.getAllByRole('img', { name: /de 5 estrellas$/ }).length).toBeGreaterThanOrEqual(3)
    expect(screen.queryByText(/Vecina de Prueba/)).not.toBeInTheDocument()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-TIE-014-02 · la API no manda el RUT ni los documentos, y la pantalla no los muestra', async () => {
    // Cuando
    const { data } = await clienteApi.GET('/api/v1/tiendas/{tiendaId}', {
      params: { path: { tiendaId: TIENDAS_SPRINT_2.la5 } },
    })
    abrir(`/tiendas/${TIENDAS_SPRINT_2.la5}`)

    // Entonces
    expect(Object.keys(data ?? {}).sort()).toEqual(
      [
        'categoriaId',
        'domicilio',
        'estaAbierta',
        'horario',
        'id',
        'latitud',
        'longitud',
        'mediosPago',
        'nombre',
        'telefono',
      ].sort(),
    )
    expect(await screen.findByRole('heading', { level: 1, name: 'Droguería La 5' })).toBeInTheDocument()
    expect(screen.queryByText(/RUT|documento/i)).not.toBeInTheDocument()
  })

  it('una tienda sin reseñas dice «Sin reseñas todavía» y una sin domicilio, que es solo para recoger', async () => {
    // Cuando: Farmacia Popular no hace domicilio y nadie la ha calificado
    abrir(`/tiendas/${TIENDAS_SPRINT_2.popular}`)

    // Entonces
    expect(await screen.findByRole('heading', { level: 1, name: 'Farmacia Popular' })).toBeInTheDocument()
    expect(await screen.findAllByText('Sin reseñas todavía')).toHaveLength(2)
    expect(screen.getByText('No hace domicilio: solo para recoger en la tienda.')).toBeInTheDocument()
  })

  it('una tienda que no está aprobada responde TIE-006 y se dice que no está disponible', async () => {
    // Cuando
    const pendiente = base.tiendas.find((t) => t.estado === 'PENDIENTE')!
    abrir(`/tiendas/${pendiente.id}`)

    // Entonces
    expect(await screen.findByText('Esta tienda no está disponible.')).toBeInTheDocument()
    expect(screen.queryByRole('heading', { level: 1 })).not.toBeInTheDocument()
  })

  it('desde una respuesta, «Ver la tienda» abre su detalle', async () => {
    // Dado: Botica San José respondió la solicitud
    const usuario = userEvent.setup({ delay: null })
    const solicitudId = crearSolicitud()
    abrir(`/solicitudes/${solicitudId}`)

    // Cuando
    await usuario.click(await screen.findByRole('link', { name: 'Ver la tienda Botica San José' }))

    // Entonces
    expect(await screen.findByRole('heading', { level: 1, name: 'Botica San José' })).toBeInTheDocument()
  })
})
