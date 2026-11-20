import { Star } from 'lucide-react'
import { Link } from 'react-router'
import type { Orden } from '@/features/ordenes/api'
import type { VistaOrden } from '@/features/ordenes/presentacion'
import { formatearFechaHora } from '@/services/formato'
import { useResenaDeOrden } from './api'
import { Estrellas } from './Estrellas'
import { textosReputacion as textos } from './textos'

/**
 * HU-034 en el pedido RESUELTA: el cliente ve su calificación o la invitación a calificar (una sola vez); la tienda
 * ve la calificación que le dejaron. Antes de resolverse no se muestra nada.
 */
export function ResenaDelPedido({ orden, vista }: { orden: Orden; vista: VistaOrden }) {
  const resuelta = orden.estado === 'RESUELTA'
  const resena = useResenaDeOrden(orden.id, resuelta)
  if (!resuelta || resena.isPending || resena.isError) return null

  if (!resena.data) {
    if (vista === 'tienda') return <p className="text-texto-suave">{textos.clienteSinCalificar}</p>
    return (
      <div className="flex flex-col gap-3 rounded-control border-2 border-primario p-4">
        <p className="font-medium">{textos.invitacionCalificar}</p>
        <Link
          to={`/pedidos/${orden.id}/calificar`}
          className="inline-flex min-h-tactil items-center justify-center gap-2 rounded-control bg-primario px-4 font-semibold text-primario-texto hover:bg-primario-hover"
        >
          <Star aria-hidden="true" className="size-5" />
          {textos.calificarA(orden.tienda.nombre)}
        </Link>
      </div>
    )
  }

  const { estrellas, comentario, fecha } = resena.data
  return (
    <section aria-labelledby={`resena-${orden.id}`} className="flex flex-col gap-1">
      <h2 id={`resena-${orden.id}`} className="text-lg font-semibold">
        {vista === 'cliente' ? textos.tuCalificacion : textos.calificacionDelCliente}
      </h2>
      <p className="flex flex-wrap items-center gap-2">
        <Estrellas cantidad={estrellas} />
        <span>{textos.estrellasDeCinco(estrellas)}</span>
        <span className="text-texto-suave">· {textos.fechaResena(formatearFechaHora(fecha))}</span>
      </p>
      {comentario ? <p className="break-words">«{comentario}»</p> : null}
    </section>
  )
}
