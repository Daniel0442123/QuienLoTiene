import type { ReactNode } from 'react'
import { esErrorApi } from '@/services/errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { Boton } from './ui/Boton'

/**
 * Los cuatro estados obligatorios de todo componente con datos del servidor (DECISIONES-FRONTEND sección 10.2):
 * cargando, vacío, error y deshabilitado (este último lo maneja cada componente con su razón visible).
 */

export function EsqueletoLista({ filas = 3, etiqueta }: { filas?: number; etiqueta: string }) {
  return (
    <div role="status" aria-label={etiqueta} className="flex flex-col gap-3">
      {Array.from({ length: filas }, (_, indice) => (
        <div key={indice} className="h-20 animate-pulse rounded-control bg-borde-suave" />
      ))}
    </div>
  )
}

export function EstadoVacio({ titulo, children }: { titulo: string; children?: ReactNode }) {
  return (
    <div className="rounded-control border border-borde-suave bg-superficie p-6 text-center">
      <p className="font-semibold">{titulo}</p>
      {children ? <div className="mt-1 text-texto-suave">{children}</div> : null}
    </div>
  )
}

export function EstadoError({ error, alReintentar }: { error: unknown; alReintentar?: () => void }) {
  const mensaje = esErrorApi(error) ? mensajeParaUsuario(error) : 'Algo salió mal. Inténtalo de nuevo.'
  const idCorrelacion = esErrorApi(error) && debeMostrarIdCorrelacion(error) ? error.idCorrelacion : undefined
  return (
    <div role="alert" className="rounded-control border border-peligro p-4">
      <p className="font-medium text-peligro">{mensaje}</p>
      {idCorrelacion ? <p className="mt-1 text-sm text-texto-suave">Código para soporte: {idCorrelacion}</p> : null}
      {alReintentar ? (
        <Boton variante="secundario" className="mt-3" onClick={alReintentar}>
          Reintentar
        </Boton>
      ) : null}
    </div>
  )
}
