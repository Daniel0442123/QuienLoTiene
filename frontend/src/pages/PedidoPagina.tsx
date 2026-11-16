import { ChevronLeft } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { Link, useParams } from 'react-router'
import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { useConfirmarRecepcion, useOrden, type Orden } from '@/features/ordenes/api'
import { BotonWhatsapp } from '@/features/ordenes/BotonWhatsapp'
import { DetalleOrden } from '@/features/ordenes/DetalleOrden'
import { LineaDeTiempo } from '@/features/ordenes/LineaDeTiempo'
import { numeroCorto } from '@/features/ordenes/presentacion'
import { textosOrdenes as textos } from '@/features/ordenes/textos'
import { useAnunciarCambiosDeEstado } from '@/features/ordenes/useAnunciarCambiosDeEstado'
import { esErrorApi } from '@/services/errores'
import { formatearHora, formatearPesos, formatearUnidades } from '@/services/formato'
import { mensajeParaUsuario } from '@/services/mensajes-error'

/** HU-028: «Recibí mi pedido» cuando la tienda lo marcó como entregado. */
function ConfirmarRecepcion({ orden }: { orden: Orden }) {
  const anunciar = useAnunciar()
  const recepcion = useConfirmarRecepcion(orden.id)
  const entregado = orden.historial.find((cambio) => cambio.estado === 'ENTREGADO')
  const error = recepcion.error
  return (
    <div className="flex flex-col gap-3 rounded-control border-2 border-primario p-4">
      <p className="font-medium">
        {textos.entregadoPregunta(orden.tienda.nombre, entregado ? formatearHora(entregado.fecha) : '')}
      </p>
      {error ? (
        <p role="alert" className="font-medium text-peligro">
          {esErrorApi(error) ? mensajeParaUsuario(error) : textos.errorRecibir}
        </p>
      ) : null}
      <Boton
        type="button"
        ancho="completo"
        enviando={recepcion.isPending}
        textoEnviando={textos.botonRecibiendo}
        onClick={() => recepcion.mutate(undefined, { onSuccess: () => anunciar(textos.pedidoRecibido) })}
      >
        {textos.botonRecibi}
      </Boton>
    </div>
  )
}

/** /pedidos/:ordenId · HU-024, HU-027, HU-028 y HU-032: el pedido del cliente, cómo va y qué puede hacer. */
export function PedidoPagina() {
  const { ordenId = '' } = useParams()
  const orden = useOrden(ordenId)
  const titulo = useRef<HTMLHeadingElement>(null)
  useAnunciarCambiosDeEstado(orden.data?.estado)

  useEffect(() => {
    document.title = `${textos.tituloPedido} · ¿Quién lo tiene?`
    titulo.current?.focus()
  }, [])

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <div className="flex flex-col gap-1">
        <Link
          to="/pedidos"
          className="inline-flex min-h-tactil items-center gap-1 self-start text-primario underline underline-offset-4 hover:no-underline"
        >
          <ChevronLeft aria-hidden="true" className="size-5" />
          {textos.volverAMisPedidos}
        </Link>
        <h1 ref={titulo} tabIndex={-1} className="text-2xl font-bold">
          {textos.tituloPedido}
        </h1>
        {orden.data ? <p className="text-texto-suave">{textos.numeroPedido(numeroCorto(orden.data.id))}</p> : null}
      </div>

      {orden.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargandoPedido} />
      ) : orden.isError ? (
        <EstadoError error={orden.error} alReintentar={() => void orden.refetch()} />
      ) : (
        <>
          <div className="flex flex-col gap-1">
            <p className="text-xl font-semibold">{textos.estados[orden.data.estado]}</p>
            <p className="break-words">
              {orden.data.tienda.nombre} ·{' '}
              {textos.resumenPedido(
                orden.data.producto,
                formatearUnidades(orden.data.cantidad),
                formatearPesos(orden.data.totalEnPesos),
              )}
            </p>
            <p className="text-texto-suave">
              {orden.data.modalidad === 'DOMICILIO' ? textos.entregaDomicilio : textos.entregaRecoger}
            </p>
          </div>
          {orden.data.estado === 'ENTREGADO' ? <ConfirmarRecepcion orden={orden.data} /> : null}
          <LineaDeTiempo orden={orden.data} titulo={textos.pasos} />
          <BotonWhatsapp ordenId={orden.data.id} esActiva={orden.data.esActiva} />
          <DetalleOrden orden={orden.data} />
        </>
      )}
    </section>
  )
}
