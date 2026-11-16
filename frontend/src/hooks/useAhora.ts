import { useEffect, useState } from 'react'

/**
 * La hora actual, renovada cada `intervaloMs`. Sirve para textos que cambian solos, como «hace 4 min» o «Vence en
 * 12 min», sin leer la hora durante el render (React lo considera impuro).
 */
export function useAhora(intervaloMs = 30_000): Date {
  const [ahora, setAhora] = useState(() => new Date())
  useEffect(() => {
    const temporizador = window.setInterval(() => setAhora(new Date()), intervaloMs)
    return () => window.clearInterval(temporizador)
  }, [intervaloMs])
  return ahora
}
