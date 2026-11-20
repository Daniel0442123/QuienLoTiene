import { act, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { FormularioPublicarSolicitud } from '@/features/solicitudes/FormularioPublicarSolicitud'
import { DURACION_DESHACER_MS } from '@/hooks/useDescartarConDeshacer'
import { base, CODIGO_TOTP_DE_PRUEBA, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { iniciarSesion, limpiarSesion, verificarSegundoFactor } from '@/services/sesion'
import { CatalogoAdminPagina } from './CatalogoAdminPagina'

vi.mock('@/components/MapaUbicacion', () => ({ default: () => null }))

async function entrarComoAdministrador() {
  limpiarSesion()
  await iniciarSesion(CORREOS_DE_PRUEBA.administrador, FRASE_DE_PRUEBA)
  await verificarSegundoFactor(CODIGO_TOTP_DE_PRUEBA)
}

const interruptor = (nombre: string) => screen.findByRole('switch', { name: new RegExp(`^${nombre} · `) })
const listaRestringidos = () =>
  within(screen.getByRole('region', { name: 'Productos restringidos' })).getAllByRole('listitem')

describe('HU-038 · Categorías y productos restringidos · CP-CAT-038-01, CP-CAT-038-02', () => {
  beforeEach(entrarComoAdministrador)
  afterEach(() => vi.useRealTimers())

  it('no tiene violaciones de accesibilidad y muestra el estado de cada categoría en texto', async () => {
    // Cuando
    const { container } = renderizarConProveedores(<CatalogoAdminPagina />)

    // Entonces
    expect(await interruptor('Farmacia')).toBeChecked()
    expect(screen.getByText('Farmacia ·', { exact: false })).toHaveTextContent('Farmacia · activa')
    expect(screen.getByRole('navigation', { name: 'Administración' })).toBeInTheDocument()
    expect(await screen.findByText('amoxicilina')).toBeInTheDocument()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-CAT-038-01 · desactivar Ferretería hace que deje de aparecer al publicar', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    const { unmount } = renderizarConProveedores(<CatalogoAdminPagina />)

    // Cuando
    await usuario.click(await interruptor('Ferretería'))

    // Entonces: queda inactiva, dicho en texto, y se anuncia
    await vi.waitFor(async () => expect(await interruptor('Ferretería')).not.toBeChecked())
    expect(await screen.findByText('Ferretería quedó inactiva: ya no aparece al publicar.')).toBeInTheDocument()
    expect(base.categorias.find((c) => c.nombre === 'Ferretería')?.activa).toBe(false)

    // Y el cliente ya no la ve al publicar
    unmount()
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={vi.fn()} />)
    expect(await screen.findByRole('radio', { name: 'Farmacia' })).toBeInTheDocument()
    expect(screen.queryByRole('radio', { name: 'Ferretería' })).not.toBeInTheDocument()
  })

  it('CP-CAT-038-02 · agregar «Amoxicilina» la deja en la lista (normalizada) y las solicitudes nuevas avisan', async () => {
    // Dado: la palabra no está
    base.restringidos = base.restringidos.filter((r) => r.palabraClave !== 'amoxicilina')
    const usuario = userEvent.setup({ delay: null })
    const { unmount } = renderizarConProveedores(<CatalogoAdminPagina />)
    await screen.findByText('clonazepam')

    // Cuando
    await usuario.type(screen.getByRole('textbox', { name: 'Agregar producto restringido' }), 'Amoxicilina')
    await usuario.click(screen.getByRole('button', { name: 'Agregar' }))

    // Entonces
    expect(await screen.findByText('«amoxicilina» quedó en la lista de productos restringidos.')).toBeInTheDocument()
    expect(await screen.findByText('amoxicilina')).toBeInTheDocument()
    expect(screen.getByRole('textbox', { name: 'Agregar producto restringido' })).toHaveValue('')

    // Y al publicar «Amoxicilina 500 mg» el cliente ve el aviso (HU-023)
    unmount()
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.cliente, FRASE_DE_PRUEBA)
    renderizarConProveedores(<FormularioPublicarSolicitud alPublicar={vi.fn()} />)
    await usuario.type(await screen.findByRole('textbox', { name: '¿Qué estás buscando?' }), 'Amoxicilina 500 mg')
    expect(await screen.findByText('Este medicamento necesita fórmula médica')).toBeInTheDocument()
  })

  it('CAT-002 y la validación: repetida o muy corta se marca en el campo', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    renderizarConProveedores(<CatalogoAdminPagina />)
    const campo = await screen.findByRole('textbox', { name: 'Agregar producto restringido' })

    // Cuando: una sola letra
    await usuario.type(campo, 'a')
    await usuario.click(screen.getByRole('button', { name: 'Agregar' }))

    // Entonces
    expect(await screen.findByText('Escribe al menos 2 caracteres.')).toBeInTheDocument()
    expect(campo).toHaveAttribute('aria-invalid', 'true')

    // Cuando: una que ya está, con mayúscula y tilde
    await usuario.clear(campo)
    await usuario.type(campo, 'Tramadól')
    await usuario.click(screen.getByRole('button', { name: 'Agregar' }))

    // Entonces
    expect(await screen.findByText('Esa palabra ya está en la lista.')).toBeInTheDocument()
    expect(base.restringidos.filter((r) => r.palabraClave === 'tramadol')).toHaveLength(1)
  })

  it('«Quitar» saca la palabra con deshacer; si no se deshace, se borra a los 8 s', async () => {
    // Dado
    vi.useFakeTimers({ shouldAdvanceTime: true })
    const usuario = userEvent.setup({ delay: null, advanceTimers: vi.advanceTimersByTime })
    renderizarConProveedores(<CatalogoAdminPagina />)
    await screen.findByText('tramadol')

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'Quitar tramadol' }))

    // Entonces: sale de la lista al instante, con el aviso y el foco en «Deshacer»
    expect(screen.queryByText('tramadol')).not.toBeInTheDocument()
    expect(screen.getByText('Quitaste «tramadol».')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Deshacer' })).toHaveFocus()
    expect(base.restringidos.some((r) => r.palabraClave === 'tramadol')).toBe(true)

    // Cuando: pasa el tiempo
    await act(() => vi.advanceTimersByTimeAsync(DURACION_DESHACER_MS))

    // Entonces
    await vi.waitFor(() => expect(base.restringidos.some((r) => r.palabraClave === 'tramadol')).toBe(false))
    expect(listaRestringidos().map((item) => item.textContent)).not.toContain(expect.stringContaining('tramadol'))
  })

  it('«Deshacer» la devuelve a la lista sin borrarla', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    renderizarConProveedores(<CatalogoAdminPagina />)
    await screen.findByText('clonazepam')
    await usuario.click(screen.getByRole('button', { name: 'Quitar clonazepam' }))

    // Cuando
    await usuario.click(screen.getByRole('button', { name: 'Deshacer' }))

    // Entonces
    expect(await screen.findByText('clonazepam')).toBeInTheDocument()
    expect(await screen.findByText('«clonazepam» volvió a la lista.')).toBeInTheDocument()
    expect(base.restringidos.some((r) => r.palabraClave === 'clonazepam')).toBe(true)
  })
})
