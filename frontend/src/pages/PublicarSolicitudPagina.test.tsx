import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { InicioSesionPagina } from './InicioSesionPagina'
import { PublicarSolicitudPagina } from './PublicarSolicitudPagina'

vi.mock('@/components/MapaUbicacion', () => ({ default: () => null }))

async function publicar(producto: string) {
  const usuario = userEvent.setup()
  await screen.findByRole('radio', { name: /Casa/ })
  await usuario.type(screen.getByRole('textbox', { name: '¿Qué estás buscando?' }), producto)
  await usuario.click(await screen.findByRole('radio', { name: 'Farmacia' }))
  await usuario.click(screen.getByRole('button', { name: 'Preguntar' }))
  return usuario
}

describe('HU-016 · Esperando respuestas después de publicar', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })

  it('dice a cuántas tiendas se les preguntó, hasta cuándo y a qué distancia, con el foco en el título', async () => {
    // Dado
    const { container } = renderizarConProveedores(<PublicarSolicitudPagina />)

    // Cuando
    await publicar('Paracetamol 500 mg')

    // Entonces
    const titulo = await screen.findByRole('heading', { level: 1, name: 'Esperando respuestas' })
    expect(titulo).toHaveFocus()
    expect(screen.getByText('Le preguntamos a 1 tienda cercana si tiene «Paracetamol 500 mg».')).toBeInTheDocument()
    expect(screen.getByText(/Las tiendas pueden responder hasta las .* \(en 30 min\)\./)).toBeInTheDocument()
    expect(screen.getByText('Buscamos a 3 km a la redonda.')).toBeInTheDocument()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('estado vacío · si ninguna tienda estaba cerca lo dice y sugiere qué hacer', async () => {
    // Dado
    servidorSimulado.use(
      http.post('/api/v1/solicitudes', async ({ request }) => {
        const cuerpo = (await request.json()) as { producto: string }
        return HttpResponse.json(
          {
            id: 's-1',
            categoriaId: 'c-1',
            producto: cuerpo.producto,
            cantidad: 1,
            modalidad: 'RECOGER',
            esUrgente: false,
            ubicacion: { latitud: 2.44, longitud: -76.6 },
            radioEnMetros: 3000,
            estado: 'ABIERTA',
            fechaPublicacion: new Date().toISOString(),
            fechaVencimiento: new Date(Date.now() + 30 * 60_000).toISOString(),
            tiendasNotificadas: 0,
          },
          { status: 201 },
        )
      }),
    )
    renderizarConProveedores(<PublicarSolicitudPagina />)

    // Cuando
    await publicar('Repuesto raro')

    // Entonces
    expect(await screen.findByText(/ninguna tienda abierta de esa categoría está cerca/)).toBeInTheDocument()
    expect(screen.getByText(/Prueba más tarde o cambia la ubicación/)).toBeInTheDocument()
  })

  it('«Hacer otra pregunta» vuelve al formulario vacío', async () => {
    // Dado
    renderizarConProveedores(<PublicarSolicitudPagina />)
    const usuario = await publicar('Paracetamol')

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Hacer otra pregunta' }))

    // Entonces
    expect(screen.getByRole('textbox', { name: '¿Qué estás buscando?' })).toHaveValue('')
  })

  it('RNF-027 · al entrar, el cliente llega directo a preguntar', async () => {
    // Dado
    limpiarSesion()
    const usuario = userEvent.setup()
    renderizarConProveedores(
      <Routes>
        <Route path="/entrar" element={<InicioSesionPagina />} />
        <Route path="/solicitudes/nueva" element={<PublicarSolicitudPagina />} />
      </Routes>,
      { ruta: '/entrar' },
    )

    // Cuando
    await usuario.type(screen.getByRole('textbox', { name: 'Correo' }), CORREOS_DE_PRUEBA.cliente)
    await usuario.type(screen.getByLabelText('Frase de contraseña'), FRASE_DE_PRUEBA)
    await usuario.click(screen.getByRole('button', { name: 'Entrar' }))

    // Entonces
    expect(await screen.findByRole('heading', { level: 1, name: '¿Qué estás buscando?' })).toBeInTheDocument()
  })
})
