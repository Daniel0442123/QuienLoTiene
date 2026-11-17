import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it } from 'vitest'
import { CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { crearSolicitud, MINUTO } from '@/pruebas/solicitudes'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { SolicitudClientePagina } from './SolicitudClientePagina'

function abrir(solicitudId: string) {
  return renderizarConProveedores(
    <Routes>
      <Route path="/solicitudes/:solicitudId" element={<SolicitudClientePagina />} />
    </Routes>,
    { ruta: `/solicitudes/${solicitudId}` },
  )
}

const tiendasEnOrden = () =>
  screen.getAllByRole('article').map((tarjeta) => within(tarjeta).getByRole('heading').textContent)

describe('HU-020 · Ver y ordenar las respuestas · CP-SOL-020-02, CP-SOL-020-03', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })

  it('no tiene violaciones de accesibilidad detectables y pone el foco en el título', async () => {
    const { container } = abrir(crearSolicitud())
    expect(await screen.findByText('2 respuestas')).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 1, name: 'Respuestas a tu solicitud' })).toHaveFocus()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-SOL-020-03 · marca la alternativa en texto y dice qué ofrece; muestra domicilio y total', async () => {
    // Cuando
    abrir(crearSolicitud())

    // Entonces
    const popular = await screen.findByRole('article', { name: 'Farmacia Popular' })
    expect(popular).toHaveTextContent('Tengo algo parecido')
    expect(popular).toHaveTextContent('Ofrece: Acetaminofén 500 mg, marca genérica')
    expect(popular).toHaveTextContent('Solo para recoger')
    const botica = screen.getByRole('article', { name: 'Botica San José' })
    expect(botica).toHaveTextContent('Sí lo tengo')
    expect(botica).toHaveTextContent(/Domicilio \$\s3\.000/)
    expect(botica).toHaveTextContent(/Total \$\s5\.500/)
    expect(botica).toHaveTextContent(/\$\s2\.500 c\/u/)
    expect(botica).toHaveTextContent('Listo en 20 min')
  })

  it('CP-SOL-020-02 · ordena por precio de entrada y por distancia al elegirlo', async () => {
    // Dado
    const usuario = userEvent.setup()
    abrir(crearSolicitud())
    await screen.findByText('2 respuestas')
    expect(tiendasEnOrden()).toEqual(['Farmacia Popular', 'Botica San José'])
    expect(screen.getByRole('radio', { name: 'Precio' })).toBeChecked()

    // Cuando
    await usuario.click(screen.getByRole('radio', { name: 'Distancia' }))

    // Entonces
    await screen.findByRole('radio', { name: 'Distancia', checked: true })
    await expect.poll(tiendasEnOrden).toEqual(['Botica San José', 'Farmacia Popular'])
  })

  it('sin respuestas todavía muestra el estado vacío y dice que avisa en esta pantalla', async () => {
    // Cuando
    abrir(crearSolicitud({}, false))

    // Entonces
    expect(await screen.findByText('Todavía no hay respuestas')).toBeInTheDocument()
    expect(screen.getByText('Te avisamos en esta pantalla apenas una tienda conteste.')).toBeInTheDocument()
    expect(screen.getByText(/Vence en 2\d min/)).toBeInTheDocument()
  })
})

describe('HU-021 · Solicitud vencida, lado del cliente · CP-SOL-021-01', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })

  it('CP-SOL-021-01 · al vencer dice «Vencida» en texto, que ya no recibe respuestas y ofrece publicar de nuevo', async () => {
    // Dado: venció hace 2 minutos sin que nadie respondiera
    const id = crearSolicitud({ fechaVencimiento: new Date(Date.now() - 2 * MINUTO).toISOString() }, false)

    // Cuando
    const { container } = abrir(id)

    // Entonces
    expect(await screen.findByText('Vencida')).toBeInTheDocument()
    expect(screen.getByText(/Tu solicitud venció a las .* y ya no recibe respuestas\./)).toBeInTheDocument()
    expect(screen.getByText('Ninguna tienda la tenía disponible.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Publicar de nuevo' })).toHaveAttribute('href', '/solicitudes/nueva')
    expect(screen.queryByText('Todavía no hay respuestas')).not.toBeInTheDocument()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('una solicitud de otro cliente responde «no disponible» sin mostrar nada de ella (SOL-003)', async () => {
    // Cuando
    abrir('a5b6c7d8-0001-4e2f-9a3b-1c2d3e4f5a01')

    // Entonces
    expect(await screen.findByText(/Esta solicitud ya no está disponible/)).toBeInTheDocument()
    expect(screen.queryByText('Paracetamol 500 mg')).not.toBeInTheDocument()
  })
})
