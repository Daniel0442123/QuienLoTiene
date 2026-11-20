import { useCallback, useEffect, useRef, useState } from 'react'

/**
 * Tiempo para deshacer «No lo tengo». DECISIONES-FRONTEND §8 (regla 7): el aviso dura al menos 5 s; se deja en 8 s para
 * quien lee con lector de pantalla o se demora en ubicar el botón.
 */
export const DURACION_DESHACER_MS = 8_000

/**
 * Acción en un toque con deshacer en vez de diálogo (DECISIONES-FRONTEND §8): el elemento sale de la lista al instante
 * y la petición se envía cuando pasa el tiempo, cuando se cierra el aviso, cuando se descarta otro o cuando la persona
 * sale de la pantalla. Lo usan «No lo tengo» (HU-018, decisión 3 del contrato del Sprint 2: el backend no permite
 * borrar respuestas) y quitar un producto restringido (HU-038).
 */
export function useDescartarConDeshacer<T>(enviar: (elemento: T) => void, duracionMs = DURACION_DESHACER_MS) {
  const [pendiente, setPendiente] = useState<T | null>(null)
  const pendienteActual = useRef<T | null>(null)
  const temporizador = useRef<number | undefined>(undefined)
  const enviarActual = useRef(enviar)

  useEffect(() => {
    enviarActual.current = enviar
  }, [enviar])

  const soltar = useCallback((): T | null => {
    window.clearTimeout(temporizador.current)
    const elemento = pendienteActual.current
    pendienteActual.current = null
    setPendiente(null)
    return elemento
  }, [])

  /** Envía ya lo que estaba pendiente (cerrar el aviso o se cumplió el tiempo). */
  const confirmar = useCallback(() => {
    const elemento = soltar()
    if (elemento !== null) enviarActual.current(elemento)
  }, [soltar])

  const descartar = useCallback(
    (elemento: T) => {
      confirmar()
      pendienteActual.current = elemento
      setPendiente(elemento)
      temporizador.current = window.setTimeout(confirmar, duracionMs)
    },
    [confirmar, duracionMs],
  )

  /** Cancela el envío y devuelve lo que estaba pendiente, para volver a mostrarlo. */
  const deshacer = useCallback(() => soltar(), [soltar])

  // Al salir de la pantalla no se pierde la respuesta: se envía lo pendiente
  useEffect(
    () => () => {
      window.clearTimeout(temporizador.current)
      const elemento = pendienteActual.current
      pendienteActual.current = null
      if (elemento !== null) enviarActual.current(elemento)
    },
    [],
  )

  return { pendiente, descartar, deshacer, confirmar }
}
