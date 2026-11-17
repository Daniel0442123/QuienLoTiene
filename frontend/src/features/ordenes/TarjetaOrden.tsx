import { ChevronRight } from 'lucide-react'
import { Link } from 'react-router'
import { formatearPesos, formatearUnidades } from '@/services/formato'
import type { Orden } from './api'
import { numeroCorto } from './presentacion'
import { textosOrdenes as textos } from './textos'

/** Una orden en la lista (HU-027): todo el renglón es el enlace a su detalle; el estado va en texto. */
export function TarjetaOrden({ orden, ruta, contraparte }: { orden: Orden; ruta: string; contraparte: string }) {
  return (
    <Link
      to={ruta}
      className="flex min-h-tactil items-center gap-3 rounded-control border border-borde p-4 hover:bg-superficie"
    >
      <span className="flex flex-1 flex-col gap-1">
        <span className="font-semibold break-words">{orden.producto}</span>
        <span className="break-words text-texto-suave">
          {contraparte} · {textos.numeroPedido(numeroCorto(orden.id))}
        </span>
        <span className="text-texto-suave">
          {formatearUnidades(orden.cantidad)} · {formatearPesos(orden.totalEnPesos)}
        </span>
        <span className={orden.esActiva ? 'font-semibold text-primario' : 'font-semibold text-texto-suave'}>
          {textos.estados[orden.estado]}
        </span>
      </span>
      <ChevronRight aria-hidden="true" className="size-5 shrink-0" />
    </Link>
  )
}
