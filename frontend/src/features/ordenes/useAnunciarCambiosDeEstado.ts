import { useEffect, useRef } from 'react'
import { useAnunciar } from '@/components/RegionAnuncios'
import type { EstadoOrden } from './api'
import { textosOrdenes as textos } from './textos'

/**
 * Anuncia por la región aria-live cuando la orden cambia de estado sin que la persona haga nada (la otra parte la
 * avanzó). El primer estado que se ve no se anuncia: ya está en pantalla.
 */
export function useAnunciarCambiosDeEstado(estado: EstadoOrden | undefined, mensaje = textos.estadoCambio) {
  const anunciar = useAnunciar()
  const anterior = useRef<EstadoOrden | undefined>(undefined)
  useEffect(() => {
    if (estado && anterior.current && anterior.current !== estado) anunciar(mensaje(textos.estados[estado]))
    anterior.current = estado
  }, [estado, anunciar, mensaje])
}
