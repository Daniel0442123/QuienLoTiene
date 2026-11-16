import { ChevronLeft } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router'
import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { useMisRespuestas, useSolicitudesDisponibles, type RespuestaDeTienda } from '@/features/solicitudes/api'
import { FormularioResponder } from '@/features/solicitudes/FormularioResponder'
import { textosSolicitudes as textos } from '@/features/solicitudes/textos'
import { useAhora } from '@/hooks/useAhora'
import { formatearDistancia, formatearHace, formatearHora, formatearPesos, formatearUnidades } from '@/services/formato'

function RespuestaYaEnviada({ respuesta }: { respuesta: RespuestaDeTienda }) {
  return (
    <div className="flex flex-col gap-3 rounded-control border border-borde bg-superficie p-4">
      <p className="font-medium">
        {textos.yaRespondiste(
          textos.tiposRespuesta[respuesta.tipo],
          formatearPesos(respuesta.precioEnPesos ?? 0),
          formatearUnidades(respuesta.cantidadDisponible ?? 0),
          formatearHora(respuesta.fechaRespuesta),
        )}
      </p>
      <p className="text-texto-suave">{textos.explicacionEstadoRespuesta[respuesta.estado]}</p>
    </div>
  )
}

/**
 * /tienda/solicitudes/:solicitudId · HU-019: responder «Sí lo tengo» o «Tengo algo parecido» (`?tipo=PARECIDO`).
 * La solicitud sale de la lista de nuevas; si ya no está (respondida en otro celular, vencida o elegida), se dice por qué
 * en vez de mostrar un formulario que fallaría (CP-SOL-019-04).
 */
export function ResponderSolicitudPagina() {
  const { solicitudId } = useParams()
  const [parametros] = useSearchParams()
  const navegar = useNavigate()
  const anunciar = useAnunciar()
  const disponibles = useSolicitudesDisponibles()
  const misRespuestas = useMisRespuestas()
  const titulo = useRef<HTMLHeadingElement>(null)
  const ahora = useAhora()

  const solicitud = disponibles.data?.find((s) => s.id === solicitudId)
  const yaRespondida = misRespuestas.data?.find((r) => r.solicitudId === solicitudId)
  const tipoInicial = parametros.get('tipo') === 'PARECIDO' ? 'PARECIDO' : 'SI'
  const producto = solicitud?.producto ?? yaRespondida?.producto

  // La pantalla se abre desde la lista: el foco va al título para saber dónde se está (WCAG 2.4.3)
  useEffect(() => titulo.current?.focus(), [producto])

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <div className="flex flex-col gap-2">
        <Link
          to="/tienda/solicitudes"
          className="inline-flex min-h-tactil items-center gap-1 self-start text-primario underline underline-offset-4 hover:no-underline"
        >
          <ChevronLeft aria-hidden="true" className="size-5" />
          {textos.volverASolicitudes}
        </Link>
        <h1 ref={titulo} tabIndex={-1} className="text-2xl font-bold break-words">
          {producto ?? textos.tituloSolicitudesTienda}
        </h1>
        {solicitud ? (
          <p className="text-texto-suave">
            {textos.detalleSolicitud(
              formatearUnidades(solicitud.cantidad),
              formatearDistancia(solicitud.distanciaEnMetros),
              formatearHace(solicitud.fechaPublicacion, ahora),
            )}
          </p>
        ) : null}
      </div>

      {disponibles.isPending || (!solicitud && misRespuestas.isPending) ? (
        <EsqueletoLista filas={2} etiqueta={textos.cargandoSolicitudes} />
      ) : disponibles.isError ? (
        <EstadoError error={disponibles.error} alReintentar={() => void disponibles.refetch()} />
      ) : solicitud ? (
        <FormularioResponder
          key={`${solicitud.id}-${tipoInicial}`}
          solicitud={solicitud}
          tipoInicial={tipoInicial}
          alResponder={() => {
            anunciar(textos.respuestaEnviada(solicitud.producto))
            void navegar('/tienda/solicitudes/mis-respuestas')
          }}
        />
      ) : (
        <div className="flex flex-col gap-4">
          {yaRespondida ? (
            <RespuestaYaEnviada respuesta={yaRespondida} />
          ) : (
            <EstadoVacio titulo={textos.solicitudNoDisponible}>{textos.solicitudNoDisponibleAyuda}</EstadoVacio>
          )}
          <Boton asChild variante="secundario">
            <Link to="/tienda/solicitudes">{textos.volverASolicitudesNuevas}</Link>
          </Boton>
        </div>
      )}
    </section>
  )
}
