import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it } from 'vitest'
import { CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { ResumenTiendaPagina } from './ResumenTiendaPagina'

const RUTA = '/api/v1/tiendas/me/indicadores'

/** Responde 10 · 8 · 3 en 7 días y todo en cero en 30, para ver que el selector pide el periodo correcto. */
function simularIndicadores() {
  const periodosPedidos: string[] = []
  servidorSimulado.use(
    http.get(RUTA, ({ request }) => {
      const periodo = new URL(request.url).searchParams.get('periodo') ?? '30'
      periodosPedidos.push(periodo)
      return HttpResponse.json(
        periodo === '7'
          ? {
              periodoEnDias: 7,
              recibidas: 10,
              respondidas: 8,
              respuestasConProducto: 8,
              ganadas: 3,
              porcentajeRespondiste: 80,
              porcentajeTeEligieron: 38,
            }
          : {
              periodoEnDias: 30,
              recibidas: 0,
              respondidas: 0,
              respuestasConProducto: 0,
              ganadas: 0,
              porcentajeRespondiste: null,
              porcentajeTeEligieron: null,
            },
      )
    }),
  )
  return periodosPedidos
}

describe('HU-015 · Tu resumen de la tienda', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA)
  })

  it('no tiene violaciones de accesibilidad detectables', async () => {
    simularIndicadores()
    const { container } = renderizarConProveedores(<ResumenTiendaPagina />, { ruta: '/tienda/resumen' })
    await screen.findByText('8 de 10 (80 %)')
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-TIE-015-01 · con 10 recibidas, 8 respondidas y 3 ganadas en 7 días muestra esos conteos y porcentajes', async () => {
    const periodos = simularIndicadores()

    renderizarConProveedores(<ResumenTiendaPagina />, { ruta: '/tienda/resumen' })

    expect(await screen.findByText('8 de 10 (80 %)')).toBeInTheDocument()
    expect(screen.getByText('3 de 8 (38 %)')).toBeInTheDocument()
    const conteos = screen.getAllByRole('definition').map((d) => d.textContent)
    expect(conteos).toEqual(['10', '8', '3'])
    expect(screen.getByRole('combobox', { name: 'Periodo' })).toHaveValue('7')
    expect(periodos).toEqual(['7'])
  })

  it('CP-TIE-015-02 · una tienda sin actividad en 30 días ve todo en cero y «—», sin errores', async () => {
    const usuario = userEvent.setup({ delay: null })
    const periodos = simularIndicadores()
    renderizarConProveedores(<ResumenTiendaPagina />, { ruta: '/tienda/resumen' })
    await screen.findByText('8 de 10 (80 %)')

    await usuario.selectOptions(screen.getByRole('combobox', { name: 'Periodo' }), 'Últimos 30 días')

    expect(await screen.findAllByText('Todavía no hay datos en este periodo')).toHaveLength(2)
    expect(screen.getAllByRole('definition').map((d) => d.textContent)).toEqual(['0', '0', '0'])
    expect(screen.queryByRole('button', { name: /intentar/i })).not.toBeInTheDocument()
    expect(periodos).toEqual(['7', '30'])
  })
})
