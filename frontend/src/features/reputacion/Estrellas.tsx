import { Star } from 'lucide-react'
import { cn } from '@/components/ui/utilidades'
import { textosReputacion as textos } from './textos'

/**
 * Estrellas de solo lectura. Para el lector de pantalla es una imagen con el texto «4 de 5 estrellas»: las cinco
 * figuras no se leen una por una. El número también va en texto donde importa, no solo en la forma (WCAG 1.4.1).
 */
export function Estrellas({ cantidad, tamano = 'normal' }: { cantidad: number; tamano?: 'normal' | 'pequeno' }) {
  return (
    <span role="img" aria-label={textos.estrellasDeCinco(cantidad)} className="inline-flex items-center gap-0.5">
      {[1, 2, 3, 4, 5].map((posicion) => (
        <Star
          key={posicion}
          aria-hidden="true"
          className={cn(
            tamano === 'normal' ? 'size-5' : 'size-4',
            posicion <= cantidad ? 'fill-acento text-acento' : 'text-texto-suave',
          )}
        />
      ))}
    </span>
  )
}
