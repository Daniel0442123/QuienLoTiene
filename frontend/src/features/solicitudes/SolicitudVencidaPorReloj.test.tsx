import { act, screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { aSolicitudResponse } from '@/mocks/reglas-sprint-2'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { crearSolicitud, MINUTO } from '@/pruebas/solicitudes'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import type { Solicitud } from './api'
import { EsperandoRespuestas } from './EsperandoRespuestas'

/**
 * La API real marca el vencimiento con hasta un minuto de retraso. Para simularlo, la solicitud de la simulación
 * vence mucho después (la API sigue diciendo ABIERTA) y la pantalla recibe la hora de vencimiento real.
 */
function publicadaQueVenceEn(id: string, milisegundos: number): Solicitud {
  const solicitud = aSolicitudResponse(base.solicitudes.find((s) => s.id === id)!) as Solicitud
  return { ...solicitud, fechaVencimiento: new Date(Date.now() + milisegundos).toISOString() }
}

describe('HU-021 · DEF-016 · la solicitud vencida por reloj antes de que la API la marque', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })
  afterEach(() => vi.useRealTimers())

  it('al llegar la hora, la lista deja de invitar a esperar aunque la API siga en ABIERTA', async () => {
    // Dado: faltan 2 minutos y nadie ha respondido
    vi.useFakeTimers({ shouldAdvanceTime: true })
    const id = crearSolicitud({}, false)
    renderizarConProveedores(
      <EsperandoRespuestas solicitud={publicadaQueVenceEn(id, 2 * MINUTO)} alHacerOtra={vi.fn()} />,
    )
    expect(await screen.findByText('Te avisamos en esta pantalla apenas una tienda conteste.')).toBeInTheDocument()

    // Cuando: pasa la hora de vencimiento y la API todavía dice ABIERTA
    await act(() => vi.advanceTimersByTimeAsync(2.5 * MINUTO))

    // Entonces
    expect(screen.queryByText('Te avisamos en esta pantalla apenas una tienda conteste.')).not.toBeInTheDocument()
    expect(screen.queryByText('Todavía no hay respuestas')).not.toBeInTheDocument()
    expect(screen.getByText('Vencida')).toBeInTheDocument()
    expect(screen.getByText('Ninguna tienda la tenía disponible.')).toBeInTheDocument()
  })

  it('con respuestas, al llegar la hora ya no ofrece «Elegir»', async () => {
    // Dado: dos tiendas respondieron y falta 1 minuto
    vi.useFakeTimers({ shouldAdvanceTime: true })
    const id = crearSolicitud()
    renderizarConProveedores(<EsperandoRespuestas solicitud={publicadaQueVenceEn(id, MINUTO)} alHacerOtra={vi.fn()} />)
    expect((await screen.findAllByRole('link', { name: /^Elegir/ })).length).toBeGreaterThan(0)

    // Cuando
    await act(() => vi.advanceTimersByTimeAsync(1.5 * MINUTO))

    // Entonces
    expect(screen.queryAllByRole('link', { name: /^Elegir/ })).toHaveLength(0)
    expect(screen.getByText('Ya no puedes elegir estas respuestas.')).toBeInTheDocument()
  })
})
