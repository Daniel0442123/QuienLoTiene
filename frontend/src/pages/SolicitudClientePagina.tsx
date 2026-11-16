import { ChevronLeft } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { Link, useParams } from 'react-router'
import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { useSolicitud } from '@/features/solicitudes/api'
import { RespuestasDeSolicitud } from '@/features/solicitudes/RespuestasDeSolicitud'
import { textosSolicitudes as textos } from '@/features/solicitudes/textos'
import { useAhora } from '@/hooks/useAhora'
import { formatearUnidades, minutosHasta } from '@/services/formato'

/** /solicitudes/:solicitudId · HU-020 y HU-021: mi solicitud, su estado y las respuestas de las tiendas. */
export function SolicitudClientePagina() {
  const { solicitudId = '' } = useParams()
  const solicitud = useSolicitud(solicitudId)
  const ahora = useAhora()
  const titulo = useRef<HTMLHeadingElement>(null)

  useEffect(() => {
    document.title = `${textos.tituloRespuestas} · ¿Quién lo tiene?`
    titulo.current?.focus()
  }, [])

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <div className="flex flex-col gap-2">
        <Link
          to="/solicitudes/nueva"
          className="inline-flex min-h-tactil items-center gap-1 self-start text-primario underline underline-offset-4 hover:no-underline"
        >
          <ChevronLeft aria-hidden="true" className="size-5" />
          {textos.hacerOtraPregunta}
        </Link>
        <h1 ref={titulo} tabIndex={-1} className="text-2xl font-bold">
          {textos.tituloRespuestas}
        </h1>
        {solicitud.data ? (
          <p className="break-words text-texto-suave">
            {textos.resumenSolicitud(solicitud.data.producto, formatearUnidades(solicitud.data.cantidad))}
            {solicitud.data.estado === 'ABIERTA'
              ? ` · ${textos.venceEn(minutosHasta(solicitud.data.fechaVencimiento, ahora))}`
              : null}
          </p>
        ) : null}
      </div>

      {solicitud.isPending ? (
        <EsqueletoLista filas={2} etiqueta={textos.cargandoRespuestas} />
      ) : solicitud.isError ? (
        <EstadoError error={solicitud.error} alReintentar={() => void solicitud.refetch()} />
      ) : (
        <RespuestasDeSolicitud
          solicitudId={solicitud.data.id}
          modalidad={solicitud.data.modalidad}
          fechaVencimiento={solicitud.data.fechaVencimiento}
        />
      )}
    </section>
  )
}
