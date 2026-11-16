import { Clock, Siren, Store, Truck } from 'lucide-react'
import { Link } from 'react-router'
import { Boton } from '@/components/ui/Boton'
import { formatearDistancia, formatearHace, formatearUnidades, minutosHasta } from '@/services/formato'
import type { SolicitudDisponible } from './api'
import { idTarjetaSolicitud } from './presentacion'
import { textosSolicitudes as textos } from './textos'

/**
 * Tarjeta de una solicitud nueva para la tienda (HU-018). Sin datos del cliente (CP-SOL-018-01) y con la distancia
 * aproximada «a 1,2 km» (CP-SOL-018-02). Botones en dos filas a cualquier ancho (SC-011, decisión 3): «Sí lo tengo» y
 * «No lo tengo» arriba, «Tengo algo parecido» abajo a lo ancho.
 */
export function TarjetaSolicitudDisponible({
  solicitud,
  alDescartar,
  ahora,
}: {
  solicitud: SolicitudDisponible
  alDescartar: (solicitud: SolicitudDisponible) => void
  ahora: Date
}) {
  const idTitulo = `${idTarjetaSolicitud(solicitud.id)}-titulo`
  const IconoModalidad = solicitud.modalidad === 'DOMICILIO' ? Truck : Store
  const responder = `/tienda/solicitudes/${solicitud.id}`

  return (
    <article
      id={idTarjetaSolicitud(solicitud.id)}
      tabIndex={-1}
      aria-labelledby={idTitulo}
      className={
        solicitud.esUrgente
          ? 'flex flex-col gap-3 rounded-control border-2 border-acento p-4'
          : 'flex flex-col gap-3 rounded-control border border-borde p-4'
      }
    >
      <div className="flex flex-wrap items-start justify-between gap-2">
        <h2 id={idTitulo} className="text-lg font-semibold break-words">
          {solicitud.producto}
        </h2>
        {solicitud.esUrgente ? (
          <span className="inline-flex items-center gap-1 rounded-control bg-acento px-2 py-0.5 font-semibold text-acento-texto">
            <Siren aria-hidden="true" className="size-4" />
            {textos.urgente}
          </span>
        ) : null}
      </div>

      <div className="flex flex-col gap-1 text-texto-suave">
        <p>
          {textos.detalleSolicitud(
            formatearUnidades(solicitud.cantidad),
            formatearDistancia(solicitud.distanciaEnMetros),
            formatearHace(solicitud.fechaPublicacion, ahora),
          )}
        </p>
        <p className="flex flex-wrap gap-x-4 gap-y-1">
          <span className="inline-flex items-center gap-1">
            <IconoModalidad aria-hidden="true" className="size-4 shrink-0" />
            {textos.modalidadParaTienda[solicitud.modalidad]}
          </span>
          <span className="inline-flex items-center gap-1">
            <Clock aria-hidden="true" className="size-4 shrink-0" />
            {textos.venceEn(minutosHasta(solicitud.fechaVencimiento, ahora))}
          </span>
        </p>
      </div>

      <div className="grid grid-cols-2 gap-2">
        <Boton asChild>
          <Link to={responder} aria-label={textos.botonSiDe(solicitud.producto)}>
            {textos.botonSi}
          </Link>
        </Boton>
        <Boton
          type="button"
          variante="secundario"
          aria-label={textos.botonNoDe(solicitud.producto)}
          onClick={() => alDescartar(solicitud)}
        >
          {textos.botonNo}
        </Boton>
        <Boton asChild variante="secundario" className="col-span-2">
          <Link to={`${responder}?tipo=PARECIDO`} aria-label={textos.botonParecidoDe(solicitud.producto)}>
            {textos.botonParecido}
          </Link>
        </Boton>
      </div>
    </article>
  )
}
