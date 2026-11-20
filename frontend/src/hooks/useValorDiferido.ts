import { useEffect, useState } from 'react'

/**
 * El valor después de que deja de cambiar durante `esperaMs`. Sirve para consultar mientras alguien escribe sin
 * pedir una vez por cada tecla (HU-023).
 */
export function useValorDiferido<T>(valor: T, esperaMs = 500): T {
  const [diferido, setDiferido] = useState(valor)
  useEffect(() => {
    const temporizador = window.setTimeout(() => setDiferido(valor), esperaMs)
    return () => window.clearTimeout(temporizador)
  }, [valor, esperaMs])
  return diferido
}
