import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'

/**
 * Regiones aria-live para avisos que llegan sin que el usuario haga nada (SSE, guardados, errores).
 * WCAG 4.1.3 y anexo G punto 8. DECISIONES-FRONTEND sección 2.4.
 *
 * - "cortes" (polite): llegó una respuesta, se guardó el perfil.
 * - "urgente" (assertive): solo errores que bloquean lo que la persona está haciendo.
 */
type Prioridad = 'cortes' | 'urgente'

type Anunciar = (mensaje: string, prioridad?: Prioridad) => void

const ContextoAnuncios = createContext<Anunciar | null>(null)

export function ProveedorAnuncios({ children }: { children: ReactNode }) {
  const [mensajeCortes, setMensajeCortes] = useState('')
  const [mensajeUrgente, setMensajeUrgente] = useState('')

  const anunciar = useCallback<Anunciar>((mensaje, prioridad = 'cortes') => {
    const cambiar = prioridad === 'urgente' ? setMensajeUrgente : setMensajeCortes
    // Vaciar y volver a escribir hace que el lector de pantalla repita un mensaje igual al anterior
    cambiar('')
    window.setTimeout(() => cambiar(mensaje), 50)
  }, [])

  const valor = useMemo(() => anunciar, [anunciar])

  return (
    <ContextoAnuncios.Provider value={valor}>
      {children}
      <div className="sr-only" role="status" aria-live="polite" aria-atomic="true">
        {mensajeCortes}
      </div>
      <div className="sr-only" role="alert" aria-live="assertive" aria-atomic="true">
        {mensajeUrgente}
      </div>
    </ContextoAnuncios.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAnunciar(): Anunciar {
  const anunciar = useContext(ContextoAnuncios)
  if (!anunciar) throw new Error('useAnunciar debe usarse dentro de ProveedorAnuncios')
  return anunciar
}
