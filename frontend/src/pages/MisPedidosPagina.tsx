import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import { Boton } from '@/components/ui/Boton'
import { useOrdenes, type Orden } from '@/features/ordenes/api'
import { TarjetaOrden } from '@/features/ordenes/TarjetaOrden'
import { textosOrdenes as textos } from '@/features/ordenes/textos'
import { Link } from 'react-router'

function Grupo({ titulo, ordenes }: { titulo: string; ordenes: Orden[] }) {
  if (ordenes.length === 0) return null
  return (
    <section aria-label={titulo} className="flex flex-col gap-2">
      <h2 className="text-xl font-semibold">{titulo}</h2>
      <ul className="flex flex-col gap-3">
        {ordenes.map((orden) => (
          <li key={orden.id}>
            <TarjetaOrden orden={orden} ruta={`/pedidos/${orden.id}`} contraparte={orden.tienda.nombre} />
          </li>
        ))}
      </ul>
    </section>
  )
}

/** /pedidos · HU-027: los pedidos del cliente, primero los que están en curso. */
export function MisPedidosPagina() {
  const ordenes = useOrdenes()
  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <h1 className="text-2xl font-bold">{textos.tituloMisPedidos}</h1>
      {ordenes.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargandoPedido} />
      ) : ordenes.isError ? (
        <EstadoError error={ordenes.error} alReintentar={() => void ordenes.refetch()} />
      ) : ordenes.data.length === 0 ? (
        <EstadoVacio titulo={textos.sinPedidos}>
          <p>{textos.sinPedidosAyuda}</p>
          <Boton asChild variante="secundario" className="mt-3">
            <Link to="/solicitudes/nueva">{textos.hacerUnaPregunta}</Link>
          </Boton>
        </EstadoVacio>
      ) : (
        <>
          <Grupo titulo={textos.activos} ordenes={ordenes.data.filter((o) => o.esActiva)} />
          <Grupo titulo={textos.cerrados} ordenes={ordenes.data.filter((o) => !o.esActiva)} />
        </>
      )}
    </section>
  )
}
