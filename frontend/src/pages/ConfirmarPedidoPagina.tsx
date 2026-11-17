import { ChevronLeft } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import { useAnunciar } from '@/components/RegionAnuncios'
import { FormularioConfirmarPedido } from '@/features/ordenes/FormularioConfirmarPedido'
import { textosOrdenes as textos } from '@/features/ordenes/textos'
import { useRespuestasDeSolicitud, useSolicitud } from '@/features/solicitudes/api'
import { formatearUnidades } from '@/services/formato'

/**
 * /solicitudes/:solicitudId/elegir/:respuestaId · HU-024: confirmar el pedido con la respuesta elegida. Si ya no se
 * puede elegir (vencida o ya eligió otra), lo dice en vez de mostrar un formulario que fallaría con SOL-004.
 */
export function ConfirmarPedidoPagina() {
  const { solicitudId = '', respuestaId = '' } = useParams()
  const navegar = useNavigate()
  const anunciar = useAnunciar()
  const solicitud = useSolicitud(solicitudId)
  const respuestas = useRespuestasDeSolicitud(solicitudId, 'precio')
  const titulo = useRef<HTMLHeadingElement>(null)

  useEffect(() => {
    document.title = `${textos.tituloConfirmar} · ¿Quién lo tiene?`
    titulo.current?.focus()
  }, [])

  const respuesta = respuestas.data?.respuestas.find((r) => r.id === respuestaId)
  const elegible = respuesta && respuestas.data?.estadoSolicitud === 'ABIERTA'

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <div className="flex flex-col gap-2">
        <Link
          to={`/solicitudes/${solicitudId}`}
          className="inline-flex min-h-tactil items-center gap-1 self-start text-primario underline underline-offset-4 hover:no-underline"
        >
          <ChevronLeft aria-hidden="true" className="size-5" />
          {textos.volverARespuestas}
        </Link>
        <h1 ref={titulo} tabIndex={-1} className="text-2xl font-bold">
          {textos.tituloConfirmar}
        </h1>
        {respuesta && solicitud.data ? (
          <p className="break-words text-texto-suave">
            {textos.resumenEleccion(
              respuesta.nombreTienda,
              respuesta.descripcionAlternativa ?? solicitud.data.producto,
              formatearUnidades(respuesta.cantidadDisponible),
            )}
          </p>
        ) : null}
      </div>

      {solicitud.isPending || respuestas.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargando} />
      ) : solicitud.isError || respuestas.isError ? (
        <EstadoError
          error={solicitud.error ?? respuestas.error}
          alReintentar={() => {
            void solicitud.refetch()
            void respuestas.refetch()
          }}
        />
      ) : elegible ? (
        <FormularioConfirmarPedido
          respuesta={respuesta}
          modalidadSolicitada={solicitud.data.modalidad}
          alConfirmar={(orden) => {
            anunciar(textos.pedidoConfirmado(orden.tienda.nombre))
            void navegar(`/pedidos/${orden.id}`, { replace: true })
          }}
        />
      ) : (
        <EstadoVacio titulo={textos.respuestaNoDisponible}>{textos.respuestaNoDisponibleAyuda}</EstadoVacio>
      )}
    </section>
  )
}
