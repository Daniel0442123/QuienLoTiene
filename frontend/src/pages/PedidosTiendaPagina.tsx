import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import { useOrdenes } from '@/features/ordenes/api'
import { TarjetaOrden } from '@/features/ordenes/TarjetaOrden'
import { textosOrdenes as textos } from '@/features/ordenes/textos'
import { EncabezadoSeccionTienda } from '@/features/tiendas/EncabezadoSeccionTienda'

/** /tienda/pedidos · HU-027: los pedidos que recibió la tienda, primero los que están en curso. */
export function PedidosTiendaPagina() {
  const ordenes = useOrdenes()
  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <EncabezadoSeccionTienda titulo={textos.tituloPedidosTienda} descripcion={textos.descripcionPedidosTienda} />
      {ordenes.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargandoPedido} />
      ) : ordenes.isError ? (
        <EstadoError error={ordenes.error} alReintentar={() => void ordenes.refetch()} />
      ) : ordenes.data.length === 0 ? (
        <EstadoVacio titulo={textos.sinPedidosTienda}>{textos.sinPedidosTiendaAyuda}</EstadoVacio>
      ) : (
        <ul className="flex flex-col gap-3">
          {ordenes.data.map((orden) => (
            <li key={orden.id}>
              <TarjetaOrden
                orden={orden}
                ruta={`/tienda/pedidos/${orden.id}`}
                contraparte={orden.cliente?.nombre ?? textos.cliente}
              />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
