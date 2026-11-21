import { useEffect, useId, useRef } from 'react'
import { Link } from 'react-router'
import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { cn } from '@/components/ui/utilidades'
import { useMisSolicitudes, type SolicitudDelHistorial } from '@/features/solicitudes/api'
import { BotonRepetir } from '@/features/solicitudes/BotonRepetir'
import { textosSolicitudes as textos } from '@/features/solicitudes/textos'
import { formatearFecha, formatearUnidades } from '@/services/formato'

const claseEnlace =
  'inline-flex min-h-tactil items-center text-primario underline underline-offset-4 hover:no-underline'

/** Una pregunta del historial (mockup 03): producto, estado en texto, cuándo y qué pasó, y «Repetir». */
function TarjetaHistorial({ solicitud }: { solicitud: SolicitudDelHistorial }) {
  const idTitulo = useId()
  // «Abierta» solo si de verdad recibe respuestas; una ABIERTA que ya pasó su hora se muestra como vencida
  const estado = solicitud.estaAbierta ? 'ABIERTA' : solicitud.estado === 'ABIERTA' ? 'VENCIDA' : solicitud.estado
  return (
    <article aria-labelledby={idTitulo} className="flex flex-col gap-2 rounded-control border border-borde p-4">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <h2 id={idTitulo} className="text-lg font-semibold break-words">
          {solicitud.producto}
        </h2>
        <p
          className={cn(
            'rounded-full border px-3 py-0.5 text-sm font-semibold',
            estado === 'RESUELTA'
              ? 'border-exito text-exito'
              : estado === 'ABIERTA'
                ? 'border-primario text-primario'
                : 'border-borde text-texto-suave',
          )}
        >
          {textos.estadosHistorial[estado]}
        </p>
      </div>
      <p className="text-texto-suave">
        {formatearUnidades(solicitud.cantidad)} · {textos.publicadaEl(formatearFecha(solicitud.fechaPublicacion))}
      </p>
      <p>{solicitud.ordenId ? textos.conPedido : textos.respuestasRecibidas(solicitud.respuestasRecibidas)}</p>
      <div className="flex flex-wrap gap-x-4">
        {solicitud.estaAbierta ? (
          <Link
            to={`/solicitudes/nueva?solicitud=${solicitud.id}`}
            aria-label={textos.verRespuestasDe(solicitud.producto)}
            className={claseEnlace}
          >
            {textos.verRespuestas}
          </Link>
        ) : null}
        {solicitud.ordenId ? (
          <Link to={`/pedidos/${solicitud.ordenId}`} className={claseEnlace}>
            {textos.verPedido}
          </Link>
        ) : null}
      </div>
      <BotonRepetir solicitud={solicitud} />
    </article>
  )
}

/**
 * /solicitudes · HU-008 y SC-018: todas las preguntas del cliente, de la más reciente a la más antigua, con «Ver más»
 * mientras haya otra página. Sin preguntas, invita a hacer la primera (CP-CLI-008-02).
 */
export function HistorialSolicitudesPagina() {
  const anunciar = useAnunciar()
  const historial = useMisSolicitudes()
  const titulo = useRef<HTMLHeadingElement>(null)
  const paginas = historial.data?.pages ?? []
  const solicitudes = paginas.flatMap((pagina) => pagina.solicitudes)
  const cantidadPaginas = paginas.length
  const ultimaCantidad = paginas.at(-1)?.solicitudes.length ?? 0
  const anteriores = useRef(0)

  useEffect(() => {
    document.title = `${textos.tituloHistorial} · ¿Quién lo tiene?`
    titulo.current?.focus()
  }, [])

  // Al cargar otra página se anuncia cuántas llegaron; el foco se queda en «Ver más» (o donde estaba)
  useEffect(() => {
    if (cantidadPaginas > 1 && anteriores.current > 0) {
      anunciar(textos.cargadasMas(ultimaCantidad))
    }
    anteriores.current = cantidadPaginas
  }, [cantidadPaginas, ultimaCantidad, anunciar])

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 ref={titulo} tabIndex={-1} className="text-2xl font-bold">
          {textos.tituloHistorial}
        </h1>
        <p className="text-texto-suave">{textos.introHistorial}</p>
      </div>
      {historial.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargandoHistorial} />
      ) : historial.isError ? (
        <EstadoError error={historial.error} alReintentar={() => void historial.refetch()} />
      ) : solicitudes.length === 0 ? (
        <EstadoVacio titulo={textos.sinHistorial}>
          <p>{textos.sinHistorialAyuda}</p>
          <Boton asChild className="mt-3">
            <Link to="/solicitudes/nueva">{textos.primeraPregunta}</Link>
          </Boton>
        </EstadoVacio>
      ) : (
        <>
          <ul className="flex flex-col gap-3">
            {solicitudes.map((solicitud) => (
              <li key={solicitud.id}>
                <TarjetaHistorial solicitud={solicitud} />
              </li>
            ))}
          </ul>
          {historial.hasNextPage ? (
            <Boton
              type="button"
              variante="secundario"
              enviando={historial.isFetchingNextPage}
              textoEnviando={textos.cargandoMas}
              onClick={() => void historial.fetchNextPage()}
            >
              {textos.verMas}
            </Boton>
          ) : null}
        </>
      )}
    </section>
  )
}
