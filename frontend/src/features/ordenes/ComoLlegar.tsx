import { Navigation } from 'lucide-react'
import { useTiendaPublica } from '@/features/tiendas/api'
import type { Orden } from './api'
import { enlaceComoLlegar } from './presentacion'
import { textosOrdenes as textos } from './textos'

/**
 * HU-026, CP-ORD-026-01: en una recogida activa, «Cómo llegar» abre la app de mapas con la ruta hasta la tienda.
 * La orden solo trae el id y el nombre de la tienda, así que la ubicación sale del detalle público (HU-014). Si no se
 * puede cargar, el botón no aparece: no impide nada del pedido.
 */
export function ComoLlegar({ orden }: { orden: Orden }) {
  const mostrar = orden.modalidad === 'RECOGER' && orden.esActiva
  const tienda = useTiendaPublica(orden.tienda.id, mostrar)
  if (!mostrar || !tienda.data) return null

  return (
    <a
      href={enlaceComoLlegar(tienda.data.latitud, tienda.data.longitud)}
      target="_blank"
      rel="noopener noreferrer"
      className="inline-flex min-h-tactil items-center justify-center gap-2 rounded-control bg-primario px-4 font-semibold text-primario-texto hover:bg-primario-hover"
    >
      <Navigation aria-hidden="true" className="size-5" />
      {textos.botonComoLlegar}
      <span className="sr-only"> {textos.seAbreMapas}</span>
    </a>
  )
}
