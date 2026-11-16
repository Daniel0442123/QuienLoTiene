import { useEffect, useRef } from 'react'
import { useParams } from 'react-router'
import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { useOrden } from '@/features/ordenes/api'
import { DetalleOrden } from '@/features/ordenes/DetalleOrden'
import { numeroCorto } from '@/features/ordenes/presentacion'
import { textosOrdenes as textos } from '@/features/ordenes/textos'
import { formatearUnidades } from '@/services/formato'

/** /pedidos/:ordenId · HU-024: el pedido del cliente, su estado y el total desglosado. */
export function PedidoPagina() {
  const { ordenId = '' } = useParams()
  const orden = useOrden(ordenId)
  const titulo = useRef<HTMLHeadingElement>(null)

  useEffect(() => {
    document.title = `${textos.tituloPedido} · ¿Quién lo tiene?`
    titulo.current?.focus()
  }, [])

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <div className="flex flex-col gap-1">
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
              {orden.data.tienda.nombre} · {orden.data.producto} · {formatearUnidades(orden.data.cantidad)}
            </p>
            <p className="text-texto-suave">
              {orden.data.modalidad === 'DOMICILIO' ? textos.entregaDomicilio : textos.entregaRecoger}
            </p>
          </div>
          <DetalleOrden orden={orden.data} />
        </>
      )}
    </section>
  )
}
