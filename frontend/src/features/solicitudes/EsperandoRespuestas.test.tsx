import { act, screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { aSolicitudResponse } from '@/mocks/reglas-sprint-2'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { crearSolicitud, MINUTO } from '@/pruebas/solicitudes'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import type { Solicitud } from './api'
import { EsperandoRespuestas } from './EsperandoRespuestas'

/** La solicitud tal como la devolvió la API al publicarla. */
function publicada(id: string): Solicitud {
  return aSolicitudResponse(base.solicitudes.find((s) => s.id === id)!) as Solicitud
}

describe('HU-021 · Esperando respuestas cuando la solicitud vence · DEF-008', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
  })
  afterEach(() => vi.useRealTimers())

  it('vencida: el título y el texto dicen que venció, sin «pueden responder»', async () => {
    // Dado: la pantalla quedó abierta con la solicitud recién publicada, que ya venció
    const id = crearSolicitud({ fechaVencimiento: new Date(Date.now() - 2 * MINUTO).toISOString() }, false)
    const solicitud = { ...publicada(id), estado: 'ABIERTA' as const }

    // Cuando
    renderizarConProveedores(<EsperandoRespuestas solicitud={solicitud} alHacerOtra={vi.fn()} />)

    // Entonces
    expect(await screen.findByRole('heading', { level: 1, name: 'Tu solicitud venció' })).toBeInTheDocument()
    expect(screen.getByText(/El plazo para responder terminó a las .*\./)).toBeInTheDocument()
    expect(screen.queryByText(/pueden responder/)).not.toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Esperando respuestas' })).not.toBeInTheDocument()
    expect(await screen.findByText('Vencida')).toBeInTheDocument()
  })

  it('los minutos que faltan se actualizan con el reloj y al llegar la hora dice que venció', async () => {
    // Dado: faltan 3 minutos
    vi.useFakeTimers({ shouldAdvanceTime: true })
    const id = crearSolicitud({ fechaVencimiento: new Date(Date.now() + 3 * MINUTO).toISOString() }, false)
    renderizarConProveedores(<EsperandoRespuestas solicitud={publicada(id)} alHacerOtra={vi.fn()} />)
    expect(await screen.findByText(/\(en 3 min\)/)).toBeInTheDocument()

    // Cuando: pasan 2 minutos
    await act(() => vi.advanceTimersByTimeAsync(2 * MINUTO))

    // Entonces
    expect(screen.getByText(/\(en 1 min\)/)).toBeInTheDocument()

    // Cuando: pasa la hora de vencimiento
    await act(() => vi.advanceTimersByTimeAsync(1.5 * MINUTO))

    // Entonces
    expect(screen.getByRole('heading', { level: 1, name: 'Tu solicitud venció' })).toBeInTheDocument()
    expect(screen.queryByText(/pueden responder/)).not.toBeInTheDocument()
  })
})
