import { useId, type ComponentProps } from 'react'
import { cn } from './ui/utilidades'

/**
 * Casilla de verificación accesible: toda la etiqueta es el objetivo táctil (≥ 44 px, RNF-028) y el error queda
 * asociado con aria-describedby. Se usa con React Hook Form: <CasillaVerificacion etiqueta="..." {...register('x')} />
 */
type PropiedadesCasilla = Omit<ComponentProps<'input'>, 'type'> & {
  etiqueta: string
  error?: string
}

export function CasillaVerificacion({ etiqueta, error, id, className, ...propiedades }: PropiedadesCasilla) {
  const idGenerado = useId()
  const idCampo = id ?? idGenerado
  const idError = `${idCampo}-error`

  return (
    <div className="flex flex-col gap-1">
      <label
        htmlFor={idCampo}
        className={cn(
          'flex min-h-tactil cursor-pointer items-start gap-3 rounded-control border border-borde p-3',
          error && 'border-peligro',
          className,
        )}
      >
        <input
          id={idCampo}
          type="checkbox"
          aria-invalid={error ? true : undefined}
          aria-describedby={error ? idError : undefined}
          className="mt-0.5 size-6 shrink-0 accent-primario"
          {...propiedades}
        />
        <span>{etiqueta}</span>
      </label>
      {error ? (
        <p id={idError} className="text-sm font-medium text-peligro">
          {error}
        </p>
      ) : null}
    </div>
  )
}
