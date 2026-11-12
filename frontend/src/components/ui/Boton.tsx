import { cva, type VariantProps } from 'class-variance-authority'
import { Slot } from 'radix-ui'
import type { ComponentProps } from 'react'
import { LoaderCircle } from 'lucide-react'
import { cn } from './utilidades'

/**
 * Botón base al estilo de shadcn/ui, con los tokens del proyecto.
 * Alto mínimo de 44 px (RNF-028). Mientras `enviando` es verdadero muestra el indicador dentro del botón y no
 * permite un segundo envío (DECISIONES-FRONTEND sección 9).
 */
const variantesBoton = cva(
  'inline-flex min-h-tactil min-w-tactil items-center justify-center gap-2 rounded-control px-4 font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-60',
  {
    variants: {
      variante: {
        principal: 'bg-primario text-primario-texto hover:bg-primario-hover',
        secundario: 'border border-borde bg-fondo text-texto hover:bg-superficie',
        peligro: 'bg-peligro text-peligro-texto hover:opacity-90',
        enlace: 'text-primario underline underline-offset-4 hover:no-underline',
      },
      ancho: {
        normal: '',
        completo: 'w-full',
      },
    },
    defaultVariants: { variante: 'principal', ancho: 'normal' },
  },
)

type PropiedadesBoton = ComponentProps<'button'> &
  VariantProps<typeof variantesBoton> & {
    asChild?: boolean
    enviando?: boolean
    textoEnviando?: string
  }

export function Boton({
  className,
  variante,
  ancho,
  asChild = false,
  enviando = false,
  textoEnviando,
  disabled,
  children,
  ...propiedades
}: PropiedadesBoton) {
  const Componente = asChild ? Slot.Root : 'button'
  return (
    <Componente
      className={cn(variantesBoton({ variante, ancho }), className)}
      disabled={disabled || enviando}
      aria-busy={enviando || undefined}
      {...propiedades}
    >
      {enviando ? (
        <>
          <LoaderCircle aria-hidden="true" className="size-5 animate-spin" />
          {textoEnviando ?? children}
        </>
      ) : (
        children
      )}
    </Componente>
  )
}
