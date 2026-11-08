import { useId, type ComponentProps, type ReactNode } from 'react'
import { cn } from './ui/utilidades'

/**
 * Campo de formulario accesible (DECISIONES-FRONTEND sección 9):
 * etiqueta visible, pista ANTES del error, error asociado con aria-describedby y aria-invalid.
 * Se usa con React Hook Form: <CampoTexto etiqueta="Correo" error={errors.correo?.message} {...register('correo')} />
 */
type PropiedadesCampoTexto = ComponentProps<'input'> & {
  etiqueta: string
  pista?: ReactNode
  error?: string
}

export function CampoTexto({ etiqueta, pista, error, id, className, ...propiedades }: PropiedadesCampoTexto) {
  const idGenerado = useId()
  const idCampo = id ?? idGenerado
  const idPista = `${idCampo}-pista`
  const idError = `${idCampo}-error`
  const descritoPor = [pista ? idPista : null, error ? idError : null].filter(Boolean).join(' ') || undefined

  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={idCampo} className="font-medium">
        {etiqueta}
      </label>
      {pista ? (
        <p id={idPista} className="text-sm text-texto-suave">
          {pista}
        </p>
      ) : null}
      <input
        id={idCampo}
        aria-invalid={error ? true : undefined}
        aria-describedby={descritoPor}
        className={cn(
          'min-h-tactil rounded-control border border-borde bg-fondo px-3 text-base',
          error && 'border-peligro',
          className,
        )}
        {...propiedades}
      />
      {error ? (
        <p id={idError} className="text-sm font-medium text-peligro">
          {error}
        </p>
      ) : null}
    </div>
  )
}
