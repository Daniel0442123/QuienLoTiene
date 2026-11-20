import { ChevronLeft } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { Link, useParams } from 'react-router'
import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { ChatDelPedido } from '@/features/mensajeria/ChatDelPedido'
import { textosMensajeria as textos } from '@/features/mensajeria/textos'
import { useOrden } from '@/features/ordenes/api'
import { numeroCorto, type VistaOrden } from '@/features/ordenes/presentacion'

/**
 * /pedidos/:ordenId/chat y /tienda/pedidos/:ordenId/chat · HU-033 (mockup 02): el chat del pedido con la otra parte.
 * El cliente le escribe a la tienda; la tienda, al cliente (su nombre llega solo con la orden activa).
 */
export function ChatPedidoPagina({ vista }: { vista: VistaOrden }) {
  const { ordenId = '' } = useParams()
  const orden = useOrden(ordenId)
  const titulo = useRef<HTMLHeadingElement>(null)
  const rutaPedido = vista === 'cliente' ? `/pedidos/${ordenId}` : `/tienda/pedidos/${ordenId}`
  const otraParte =
    vista === 'cliente' ? (orden.data?.tienda.nombre ?? '') : (orden.data?.cliente?.nombre ?? textos.cliente)
  const listo = Boolean(orden.data)

  useEffect(() => {
    document.title = `${textos.tituloDocumento} · ¿Quién lo tiene?`
  }, [])
  useEffect(() => {
    if (listo) titulo.current?.focus()
  }, [listo])

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <div className="flex flex-col gap-1">
        <Link
          to={rutaPedido}
          className="inline-flex min-h-tactil items-center gap-1 self-start text-primario underline underline-offset-4 hover:no-underline"
        >
          <ChevronLeft aria-hidden="true" className="size-5" />
          {textos.volverAlPedido}
        </Link>
        {orden.data ? (
          <>
            <h1 ref={titulo} tabIndex={-1} className="text-2xl font-bold break-words">
              {textos.tituloChat(otraParte)}
            </h1>
            <p className="text-texto-suave">{textos.pedido(numeroCorto(orden.data.id))}</p>
          </>
        ) : null}
      </div>
      {orden.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargando} />
      ) : orden.isError ? (
        <EstadoError error={orden.error} alReintentar={() => void orden.refetch()} />
      ) : (
        <ChatDelPedido ordenId={ordenId} otraParte={otraParte} />
      )}
    </section>
  )
}

/** Rutas sin propiedades (lazy): una para el cliente y otra para la tienda. */
export function ChatPedidoClientePagina() {
  return <ChatPedidoPagina vista="cliente" />
}

export function ChatPedidoTiendaPagina() {
  return <ChatPedidoPagina vista="tienda" />
}
