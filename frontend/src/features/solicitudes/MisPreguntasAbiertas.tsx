import { ChevronRight, MessageCircleQuestion } from 'lucide-react'
import { Link } from 'react-router'
import { useAhora } from '@/hooks/useAhora'
import { formatearUnidades, minutosHasta } from '@/services/formato'
import { useMisSolicitudes } from './api'
import { textosSolicitudes as textos } from './textos'

/**
 * SC-018 en «Mis pedidos»: las preguntas que todavía reciben respuestas (`estaAbierta`), para volver a ellas si el
 * cliente salió de la pantalla sin elegir. Cada una abre «Esperando respuestas» (`/solicitudes/nueva?solicitud=`, la de
 * DEF-011). Debajo, el enlace al historial completo. Sin ninguna pregunta todavía, no se muestra nada.
 */
export function MisPreguntasAbiertas() {
  const historial = useMisSolicitudes()
  const ahora = useAhora()
  const primera = historial.data?.pages[0]
  if (!primera || primera.solicitudes.length === 0) return null
  const abiertas = primera.solicitudes.filter((s) => s.estaAbierta)

  return (
    <section aria-labelledby="mis-preguntas" className="flex flex-col gap-2">
      <h2 id="mis-preguntas" className="text-xl font-semibold">
        {textos.tituloMisPreguntas}
      </h2>
      {abiertas.length > 0 ? (
        <>
          <p className="text-texto-suave">{textos.introMisPreguntas}</p>
          <ul className="flex flex-col gap-2">
            {abiertas.map((solicitud) => (
              <li key={solicitud.id}>
                <Link
                  to={`/solicitudes/nueva?solicitud=${solicitud.id}`}
                  className="flex min-h-tactil items-center gap-3 rounded-control border border-borde p-3 hover:bg-superficie"
                >
                  <MessageCircleQuestion aria-hidden="true" className="size-6 shrink-0 text-primario" />
                  <span className="flex flex-1 flex-col">
                    <span className="font-semibold break-words">
                      {solicitud.producto} · {formatearUnidades(solicitud.cantidad)}
                    </span>
                    <span className="text-sm text-texto-suave">
                      {textos.respuestasRecibidas(solicitud.respuestasRecibidas)} ·{' '}
                      {textos.venceEn(minutosHasta(solicitud.fechaVencimiento, ahora))}
                    </span>
                  </span>
                  <ChevronRight aria-hidden="true" className="size-5 shrink-0" />
                </Link>
              </li>
            ))}
          </ul>
        </>
      ) : null}
      <Link
        to="/solicitudes"
        className="inline-flex min-h-tactil items-center self-start text-primario underline underline-offset-4 hover:no-underline"
      >
        {textos.enlaceHistorial}
      </Link>
    </section>
  )
}
