import { useId, type ReactNode } from 'react'
import { FileCheck2, LoaderCircle } from 'lucide-react'
import { formatearTamano } from '@/services/formato'
import { cn } from './ui/utilidades'

/**
 * Campo para adjuntar un archivo con el control nativo del navegador (accesible con teclado y lector de pantalla).
 * Muestra qué archivo quedó elegido y su tamaño, la pista antes del error y el error asociado con aria-describedby.
 */
export function CampoArchivo({
  etiqueta,
  pista,
  accept,
  archivo,
  alElegir,
  error,
  procesando = false,
  textoProcesando,
  id,
  name,
  onBlur,
}: {
  etiqueta: string
  pista?: ReactNode
  accept: string
  archivo: File | null
  alElegir: (archivo: File | null) => void
  error?: string
  procesando?: boolean
  textoProcesando?: string
  id?: string
  name?: string
  onBlur?: () => void
}) {
  const idGenerado = useId()
  const idCampo = id ?? idGenerado
  const idPista = `${idCampo}-pista`
  const idElegido = `${idCampo}-elegido`
  const idError = `${idCampo}-error`
  const descritoPor =
    [pista ? idPista : null, archivo || procesando ? idElegido : null, error ? idError : null]
      .filter(Boolean)
      .join(' ') || undefined

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
        name={name}
        type="file"
        accept={accept}
        aria-invalid={error ? true : undefined}
        aria-describedby={descritoPor}
        onBlur={onBlur}
        onChange={(evento) => alElegir(evento.target.files?.[0] ?? null)}
        className={cn(
          'min-h-tactil w-full max-w-full rounded-control border border-borde bg-fondo p-1 text-base',
          'file:mr-3 file:min-h-10 file:cursor-pointer file:rounded-control file:border-0 file:bg-superficie file:px-3 file:font-semibold file:text-texto',
          error && 'border-peligro',
        )}
      />
      {procesando ? (
        <p id={idElegido} role="status" className="flex items-center gap-2 text-sm text-texto-suave">
          <LoaderCircle aria-hidden="true" className="size-4 animate-spin" />
          {textoProcesando}
        </p>
      ) : archivo ? (
        <p id={idElegido} className="flex items-center gap-2 text-sm text-exito">
          <FileCheck2 aria-hidden="true" className="size-4 shrink-0" />
          <span className="break-all">
            {archivo.name} · {formatearTamano(archivo.size)}
          </span>
        </p>
      ) : null}
      {error ? (
        <p id={idError} className="text-sm font-medium text-peligro">
          {error}
        </p>
      ) : null}
    </div>
  )
}
