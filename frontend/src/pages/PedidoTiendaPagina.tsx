import { ChevronLeft } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { Link, useParams } from 'react-router'
import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { useCambiarEstadoOrden, useOrden, type Orden } from '@/features/ordenes/api'
import { AvisoCancelado, AvisoReservaVencida, ReservaParaRecoger } from '@/features/ordenes/AvisosPedido'
import { BotonWhatsapp } from '@/features/ordenes/BotonWhatsapp'
import { CancelarPedido } from '@/features/ordenes/CancelarPedido'
import { DetalleOrden } from '@/features/ordenes/DetalleOrden'
import { LineaDeTiempo } from '@/features/ordenes/LineaDeTiempo'
import { numeroCorto, siguientePasoDeLaTienda } from '@/features/ordenes/presentacion'
import { textosOrdenes as textos } from '@/features/ordenes/textos'
import { esErrorApi } from '@/services/errores'
import { formatearUnidades } from '@/services/formato'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'

/** Datos del cliente: solo llegan mientras la orden está activa (CP-ORD-024-02, CP-ORD-028-01). */
function DatosDelCliente({ orden }: { orden: Orden }) {
  if (!orden.cliente) return <p className="text-texto-suave">{textos.datosClienteOcultos}</p>
  const { nombre, telefono, direccion } = orden.cliente
  return (
    <section aria-labelledby={`cliente-${orden.id}`} className="flex flex-col gap-1">
      <h2 id={`cliente-${orden.id}`} className="text-lg font-semibold">
        {orden.modalidad === 'DOMICILIO' ? textos.entregarA : textos.cliente}
      </h2>
      <p>{nombre}</p>
      <p>
        <a
          href={`tel:${telefono}`}
          className="inline-flex min-h-tactil items-center text-primario underline underline-offset-4 hover:no-underline"
        >
          {telefono}
        </a>
      </p>
      {direccion ? (
        <p className="break-words">
          {direccion.direccionEscrita}
          {direccion.indicaciones ? ` · ${direccion.indicaciones}` : null}
        </p>
      ) : null}
    </section>
  )
}

/** Solo el paso siguiente válido (CP-ORD-027-01, CP-ORD-027-02): un botón, sin elegir entre estados. */
function AvanzarPedido({ orden }: { orden: Orden }) {
  const anunciar = useAnunciar()
  const cambio = useCambiarEstadoOrden(orden.id)
  const siguiente = siguientePasoDeLaTienda(orden.estado, orden.modalidad)

  if (!siguiente) {
    return orden.estado === 'ENTREGADO' ? <p className="text-texto-suave">{textos.esperandoCliente}</p> : null
  }
  const error = cambio.error
  return (
    <div className="flex flex-col gap-2">
      {error ? (
        <div role="alert" className="rounded-control border border-peligro p-3 font-medium text-peligro">
          <p>{esErrorApi(error) ? mensajeParaUsuario(error) : textos.errorGeneral}</p>
          {esErrorApi(error) && debeMostrarIdCorrelacion(error) ? (
            <p className="mt-1 text-sm font-normal text-texto-suave">
              {textos.codigoSoporteOrden(error.idCorrelacion)}
            </p>
          ) : null}
        </div>
      ) : null}
      <p className="text-texto-suave">{textos.clienteVeCambios}</p>
      <Boton
        type="button"
        ancho="completo"
        enviando={cambio.isPending}
        textoEnviando={textos.avanzando}
        onClick={() =>
          cambio.mutate(siguiente, {
            onSuccess: (actualizada) => anunciar(textos.estadoGuardado(textos.estados[actualizada.estado])),
          })
        }
      >
        {textos.siguientePaso[siguiente]}
      </Boton>
    </div>
  )
}

/**
 * /tienda/pedidos/:ordenId · HU-025, HU-027, HU-029 y HU-032: el pedido de la tienda, sus datos de entrega, el paso
 * siguiente y, solo en CONFIRMADO, cancelar.
 */
export function PedidoTiendaPagina() {
  const { ordenId = '' } = useParams()
  const orden = useOrden(ordenId)
  const titulo = useRef<HTMLHeadingElement>(null)

  useEffect(() => {
    document.title = `${textos.tituloPedidosTienda} · ¿Quién lo tiene?`
    titulo.current?.focus()
  }, [])

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <div className="flex flex-col gap-1">
        <Link
          to="/tienda/pedidos"
          className="inline-flex min-h-tactil items-center gap-1 self-start text-primario underline underline-offset-4 hover:no-underline"
        >
          <ChevronLeft aria-hidden="true" className="size-5" />
          {textos.volverAPedidos}
        </Link>
        <h1 ref={titulo} tabIndex={-1} className="text-2xl font-bold">
          {orden.data ? textos.numeroPedido(numeroCorto(orden.data.id)) : textos.tituloPedidosTienda}
        </h1>
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
              {orden.data.producto} · {formatearUnidades(orden.data.cantidad)}
            </p>
            <p className="text-texto-suave">
              {orden.data.modalidad === 'DOMICILIO' ? textos.entregaDomicilio : textos.entregaRecoger}
            </p>
          </div>
          <AvisoCancelado orden={orden.data} vista="tienda" />
          <AvisoReservaVencida orden={orden.data} vista="tienda" />
          <ReservaParaRecoger orden={orden.data} vista="tienda" />
          <DatosDelCliente orden={orden.data} />
          <BotonWhatsapp ordenId={orden.data.id} esActiva={orden.data.esActiva} />
          <AvanzarPedido orden={orden.data} />
          <LineaDeTiempo orden={orden.data} titulo={textos.pasosTienda} />
          <DetalleOrden orden={orden.data} vista="tienda" />
          <CancelarPedido orden={orden.data} vista="tienda" />
        </>
      )}
    </section>
  )
}
