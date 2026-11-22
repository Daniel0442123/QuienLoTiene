import { describe, expect, it } from 'vitest'
import { Route, Routes } from 'react-router'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { AnunciadorDeRuta } from './AnunciadorDeRuta'

function renderizarConTitulo(titulo: string | null) {
  return renderizarConProveedores(
    <Routes>
      <Route
        path="*"
        element={
          <>
            <main>{titulo === null ? <p>Sin título</p> : <h1>{titulo}</h1>}</main>
            <AnunciadorDeRuta />
          </>
        }
      />
    </Routes>,
  )
}

describe('Título de la pestaña según el h1 (DECISIONES-FRONTEND §2.4)', () => {
  it('pone el h1 de la pantalla antes del nombre de la app', () => {
    renderizarConTitulo('¿Qué necesitas hoy?')
    expect(document.title).toBe('¿Qué necesitas hoy? · ¿Quién lo tiene?')
  })

  it('SC-020 · en el inicio sin sesión, cuyo h1 es el nombre de la app, no lo repite', () => {
    renderizarConTitulo('¿Quién lo tiene?')
    expect(document.title).toBe('¿Quién lo tiene?')
  })

  it('sin h1 deja solo el nombre de la app', () => {
    renderizarConTitulo(null)
    expect(document.title).toBe('¿Quién lo tiene?')
  })
})
