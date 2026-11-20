import { Circle, CircleCheck, CircleDot } from 'lucide-react'
import { cn } from '@/components/ui/utilidades'
import { formatearHora } from '@/services/formato'
import type { Orden } from './api'
import { pasosDelPedido } from './presentacion'
import { textosOrdenes as textos } from './textos'

/**
 * Pasos del pedido con la hora de cada cambio (HU-027). El paso actual va marcado con `aria-current="step"` y con
 * texto, no solo con color o ícono (§8, regla 6). Si se canceló o venció, se muestra lo que pasó en vez de los pasos.
 */
export function LineaDeTiempo({ orden, titulo }: { orden: Orden; titulo: string }) {
  const horaDe = new Map(orden.historial.map((cambio) => [cambio.estado, cambio.fecha]))
  const pasos =
    orden.estado === 'CANCELADO' || orden.estado === 'VENCIDA'
      ? orden.historial.map((cambio) => cambio.estado)
      : pasosDelPedido(orden.modalidad)
  const actual = pasos.indexOf(orden.estado)

  return (
    <section aria-labelledby={`pasos-${orden.id}`} className="flex flex-col gap-2">
      <h2 id={`pasos-${orden.id}`} className="text-lg font-semibold">
        {titulo}
      </h2>
      <ol className="flex flex-col gap-2">
        {pasos.map((estado, indice) => {
          const hecho = indice < actual
          const esActual = indice === actual
          const Icono = hecho ? CircleCheck : esActual ? CircleDot : Circle
          const hora = horaDe.get(estado)
          // HU-029: «Cancelado por el cliente» o «por la tienda» (mockup 09)
          const nombre =
            estado === 'CANCELADO' && orden.canceladaPor
              ? textos.pasoCancelado[orden.canceladaPor]
              : textos.estados[estado]
          return (
            <li
              key={estado}
              aria-current={esActual ? 'step' : undefined}
              className={cn(
                'flex items-center gap-2',
                esActual ? 'font-semibold text-primario' : hecho ? 'text-texto' : 'text-texto-suave',
              )}
            >
              <Icono aria-hidden="true" className="size-5 shrink-0" />
              <span>
                {hora ? textos.paso(nombre, formatearHora(hora)) : nombre}
                {esActual ? <span className="sr-only"> {textos.estadoActual}</span> : null}
              </span>
            </li>
          )
        })}
      </ol>
    </section>
  )
}
