import { Link } from 'react-router'
import { CircleCheck, CircleSlash, Hourglass, TimerOff, type LucideIcon } from 'lucide-react'
import type { RespuestaDeTienda } from './api'
import { resumenDeRespuesta } from './presentacion'
import { textosSolicitudes as textos } from './textos'

// C-04 B5: tipado con el campo de la respuesta, que sigue igual con los tipos generados del backend
const PRESENTACION: Record<RespuestaDeTienda['estado'], { Icono: LucideIcon; color: string }> = {
  ESPERANDO: { Icono: Hourglass, color: 'text-texto-suave' },
  ELEGIDA: { Icono: CircleCheck, color: 'text-exito' },
  NO_ELEGIDA: { Icono: CircleSlash, color: 'text-texto-suave' },
  VENCIDA: { Icono: TimerOff, color: 'text-advertencia' },
}

/** Una respuesta de la tienda con su estado en texto e ícono, no solo color (HU-021, CP-SOL-021-02). */
export function TarjetaMiRespuesta({ respuesta }: { respuesta: RespuestaDeTienda }) {
  const { Icono, color } = PRESENTACION[respuesta.estado]
  return (
    <article className="flex flex-col gap-1 rounded-control border border-borde p-4">
      <h2 className="text-lg font-semibold break-words">{respuesta.producto}</h2>
      <p>{resumenDeRespuesta(respuesta)}</p>
      {respuesta.descripcionAlternativa ? (
        <p className="break-words text-texto-suave">{respuesta.descripcionAlternativa}</p>
      ) : null}
      <p className={`mt-1 flex items-center gap-2 font-semibold ${color}`}>
        <Icono aria-hidden="true" className="size-5 shrink-0" />
        {textos.estadosRespuesta[respuesta.estado]}
      </p>
      <p className="text-texto-suave">{textos.explicacionEstadoRespuesta[respuesta.estado]}</p>
      {respuesta.ordenId ? (
        <Link
          to={`/tienda/pedidos/${respuesta.ordenId}`}
          className="inline-flex min-h-tactil items-center self-start font-semibold text-primario underline underline-offset-4 hover:no-underline"
        >
          {textos.verElPedido}
        </Link>
      ) : null}
    </article>
  )
}
