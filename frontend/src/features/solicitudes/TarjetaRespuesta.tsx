import { CircleCheck, Clock, Repeat } from 'lucide-react'
import type { ReactNode } from 'react'
import { formatearDistancia, formatearPesos } from '@/services/formato'
import type { RespuestaParaCliente } from './api'
import { textosSolicitudes as textos } from './textos'

/**
 * Tarjeta de una respuesta para el cliente (HU-020). «Tengo algo parecido» va marcada con texto e ícono, no solo con
 * color, y dice qué ofrece (CP-SOL-020-03). Sin teléfono de la tienda: el WhatsApp aparece solo con orden activa
 * (CP-MEN-032-02). `accion` es el botón para elegirla (HU-024), cuando todavía se puede.
 */
export function TarjetaRespuesta({
  respuesta,
  cantidadSolicitada,
  conDomicilio,
  accion,
}: {
  respuesta: RespuestaParaCliente
  cantidadSolicitada: number
  conDomicilio: boolean
  accion?: ReactNode
}) {
  const idTitulo = `respuesta-${respuesta.id}`
  const domicilio =
    respuesta.ofreceDomicilio && respuesta.costoDomicilioEnPesos !== null
      ? textos.domicilioCuesta(formatearPesos(respuesta.costoDomicilioEnPesos))
      : textos.soloRecoger
  const detalles = [
    textos.aDistancia(formatearDistancia(respuesta.distanciaEnMetros)),
    conDomicilio ? domicilio : null,
    respuesta.cantidadDisponible < cantidadSolicitada
      ? textos.tieneDe(respuesta.cantidadDisponible, cantidadSolicitada)
      : null,
  ].filter(Boolean)

  return (
    <article
      aria-labelledby={idTitulo}
      className={
        respuesta.esAlternativa
          ? 'flex flex-col gap-2 rounded-control border border-dashed border-borde p-4'
          : 'flex flex-col gap-2 rounded-control border border-borde p-4'
      }
    >
      <h3 id={idTitulo} className="text-lg font-semibold break-words">
        {respuesta.nombreTienda}
      </h3>
      {respuesta.esAlternativa ? (
        <p className="flex items-center gap-2 font-semibold text-acento">
          <Repeat aria-hidden="true" className="size-5 shrink-0" />
          {textos.tipoParecido}
        </p>
      ) : (
        <p className="flex items-center gap-2 font-semibold text-exito">
          <CircleCheck aria-hidden="true" className="size-5 shrink-0" />
          {textos.tipoSi}
        </p>
      )}
      {respuesta.descripcionAlternativa ? (
        <p className="break-words">{textos.ofrece(respuesta.descripcionAlternativa)}</p>
      ) : null}
      <p className="text-texto-suave">{detalles.join(' · ')}</p>
      {respuesta.tiempoEstimadoEnMinutos !== null ? (
        <p className="flex items-center gap-1 text-texto-suave">
          <Clock aria-hidden="true" className="size-4 shrink-0" />
          {textos.listoEn(respuesta.tiempoEstimadoEnMinutos)}
        </p>
      ) : null}
      <div className="flex flex-wrap items-end justify-between gap-3">
        <p className="flex flex-col">
          <span className="text-xl font-bold">{textos.total(formatearPesos(respuesta.totalEnPesos))}</span>
          <span className="text-sm text-texto-suave">
            {textos.precioPorUnidad(formatearPesos(respuesta.precioEnPesos))}
          </span>
        </p>
        {accion}
      </div>
    </article>
  )
}
