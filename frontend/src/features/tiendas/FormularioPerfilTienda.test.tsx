import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import type { MiTienda } from './api'
import { FormularioPerfilTienda } from './FormularioPerfilTienda'

/** La tienda aprobada de los datos simulados: abre de lunes a viernes de 8 a 18 y el sábado de 9 a 13. */
function tiendaSimulada(): MiTienda {
  const tienda = base.tiendas.find((t) => t.estado === 'APROBADA')
  if (!tienda) throw new Error('Falta la tienda aprobada en la simulación')
  return { ...tienda, horario: tienda.horario.map((franja) => ({ ...franja })) } as MiTienda
}
const tiendaGuardada = () => base.tiendas.find((t) => t.estado === 'APROBADA')
const botonGuardar = () => screen.getByRole('button', { name: 'Guardar cambios' })

describe('HU-009 · Perfil y horario de la tienda', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA)
  })

  it('no tiene violaciones de accesibilidad detectables', async () => {
    const { container } = renderizarConProveedores(<FormularioPerfilTienda tienda={tiendaSimulada()} />)
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('muestra el horario guardado: días abiertos con sus horas y el domingo cerrado', () => {
    // Cuando
    renderizarConProveedores(<FormularioPerfilTienda tienda={tiendaSimulada()} />)

    // Entonces
    expect(screen.getByRole('checkbox', { name: 'Lunes' })).toBeChecked()
    expect(screen.getByLabelText('Sábado: Abre')).toHaveValue('09:00')
    expect(screen.getByRole('checkbox', { name: 'Domingo' })).not.toBeChecked()
    expect(screen.queryByLabelText('Domingo: Abre')).not.toBeInTheDocument()
  })

  it('CP-TIE-009-02 · guarda el teléfono y el horario nuevos y avisa', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioPerfilTienda tienda={tiendaSimulada()} />)

    // Cuando
    const telefono = screen.getByRole('textbox', { name: 'Celular' })
    await usuario.clear(telefono)
    await usuario.type(telefono, '320 111 2233')
    await usuario.click(screen.getByRole('checkbox', { name: 'Sábado' }))
    await usuario.click(screen.getByRole('checkbox', { name: 'Domingo' }))
    await usuario.click(botonGuardar())

    // Entonces
    expect(await screen.findByText('Cambios guardados.', { selector: 'p.text-exito' })).toBeInTheDocument()
    expect(tiendaGuardada()?.telefono).toBe('3201112233')
    const dias = tiendaGuardada()?.horario.map((f) => f.diaSemana)
    expect(dias).toContain('SUNDAY')
    expect(dias).not.toContain('SATURDAY')
  })

  it('CP-TIE-009-01 · no deja guardar una franja que cierra antes de abrir', async () => {
    // Dado
    const usuario = userEvent.setup()
    const peticiones = vi.fn()
    servidorSimulado.events.on('request:start', ({ request }) => {
      if (request.method === 'PUT') peticiones()
    })
    renderizarConProveedores(<FormularioPerfilTienda tienda={tiendaSimulada()} />)

    // Cuando
    const cierreLunes = screen.getByLabelText('Lunes: Cierra')
    await usuario.clear(cierreLunes)
    await usuario.type(cierreLunes, '07:00')
    await usuario.click(botonGuardar())

    // Entonces
    expect(cierreLunes).toHaveAttribute('aria-invalid', 'true')
    expect(cierreLunes).toHaveAccessibleDescription(/después de la de apertura/)
    expect(peticiones).not.toHaveBeenCalled()
    servidorSimulado.events.removeAllListeners()
  })

  it('copia el horario del lunes a los demás días que abre', async () => {
    // Dado
    const usuario = userEvent.setup()
    renderizarConProveedores(<FormularioPerfilTienda tienda={tiendaSimulada()} />)

    // Cuando
    await usuario.click(screen.getByRole('button', { name: /Copiar el horario del lunes/ }))

    // Entonces
    expect(screen.getByLabelText('Sábado: Abre')).toHaveValue('08:00')
    expect(screen.getByLabelText('Sábado: Cierra')).toHaveValue('18:00')
  })

  it('TIE-007 · si el backend rechaza el horario lo explica arriba del horario', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(
      http.put('/api/v1/tiendas/me/perfil', () =>
        HttpResponse.json({ title: 'Horario inválido', status: 400, codigo: 'TIE-007', errores: [] }, { status: 400 }),
      ),
    )
    renderizarConProveedores(<FormularioPerfilTienda tienda={tiendaSimulada()} />)

    // Cuando
    await usuario.click(botonGuardar())

    // Entonces
    expect(await screen.findByText(/El horario tiene un error/)).toBeInTheDocument()
  })

  it('estado enviando · el botón dice que está guardando', async () => {
    // Dado
    const usuario = userEvent.setup()
    servidorSimulado.use(http.put('/api/v1/tiendas/me/perfil', () => new Promise(() => undefined)))
    renderizarConProveedores(<FormularioPerfilTienda tienda={tiendaSimulada()} />)

    // Cuando
    await usuario.click(botonGuardar())

    // Entonces
    expect(await screen.findByRole('button', { name: 'Guardando…' })).toBeDisabled()
  })
})
