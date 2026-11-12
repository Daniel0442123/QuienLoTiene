import { useId, type ComponentProps } from 'react'
import { cn } from './ui/utilidades'

/** Lista desplegable accesible con la misma forma de CampoTexto: etiqueta visible y error asociado. */
export function CampoSeleccion({
  etiqueta,
  opciones,
  error,
  ...propiedades
}: ComponentProps<'select'> & { etiqueta: string; opciones: Array<{ valor: string; texto: string }>; error?: string }) {
  const id = useId()
  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={id} className="font-medium">
        {etiqueta}
      </label>
      <select
        id={id}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? `${id}-error` : undefined}
        className={cn(
          'min-h-tactil rounded-control border border-borde bg-fondo px-3 text-base',
          error && 'border-peligro',
        )}
        {...propiedades}
      >
        {opciones.map(({ valor, texto }) => (
          <option key={valor} value={valor}>
            {texto}
          </option>
        ))}
      </select>
      {error ? (
        <p id={`${id}-error`} className="text-sm font-medium text-peligro">
          {error}
        </p>
      ) : null}
    </div>
  )
}
