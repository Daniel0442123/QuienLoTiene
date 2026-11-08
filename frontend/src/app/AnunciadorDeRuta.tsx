import { useEffect, useRef } from 'react'
import { useLocation } from 'react-router'

const NOMBRE_APP = '¿Quién lo tiene?'

/**
 * En una SPA el lector de pantalla no se entera de que cambió la pantalla. Al cambiar de ruta, este componente
 * mueve el foco al h1 de la nueva pantalla y actualiza el título de la pestaña (DECISIONES-FRONTEND sección 2.4).
 * Cada página debe tener exactamente un <h1>.
 */
export function AnunciadorDeRuta() {
  const { pathname } = useLocation()
  const esPrimeraCarga = useRef(true)

  useEffect(() => {
    const titulo = document.querySelector('main h1')
    if (titulo instanceof HTMLElement) {
      document.title = `${titulo.textContent} · ${NOMBRE_APP}`
      // En la primera carga el navegador ya ubica el foco al inicio; no se mueve para no saltar el enlace "Ir al contenido"
      if (!esPrimeraCarga.current) {
        titulo.setAttribute('tabindex', '-1')
        titulo.focus()
      }
    } else {
      document.title = NOMBRE_APP
    }
    esPrimeraCarga.current = false
  }, [pathname])

  return null
}
