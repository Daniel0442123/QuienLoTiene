import { Clock, MapPin, Store } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { EstadoVacio } from '@/components/Estados'
import { Boton } from '@/components/ui/Boton'
import { formatearDistancia, formatearHora, minutosHasta } from '@/services/formato'
import type { Solicitud } from './api'
import { textosSolicitudes as textos } from './textos'

/**
 * Después de publicar (paso 9 de la secuencia: «Esperando respuestas»). Dice a cuántas tiendas se les preguntó, hasta
 * cuándo pueden responder y a qué distancia; si no hubo ninguna, lo dice y sugiere qué hacer (estado vacío).
 * La pantalla cambia sin cambiar de ruta: el foco va al título para que el lector de pantalla lo lea.
 */
export function EsperandoRespuestas({ solicitud, alHacerOtra }: { solicitud: Solicitud; alHacerOtra: () => void }) {
  const titulo = useRef<HTMLHeadingElement>(null)
  useEffect(() => {
    document.title = `${textos.tituloEsperando} · ¿Quién lo tiene?`
    titulo.current?.focus()
  }, [])

  const hayTiendas = solicitud.tiendasNotificadas > 0

  return (
    <section className="flex flex-col gap-5">
      <h1 ref={titulo} tabIndex={-1} className="text-2xl font-bold">
        {textos.tituloEsperando}
      </h1>

      {hayTiendas ? (
        <ul className="flex flex-col gap-3">
          <li className="flex items-start gap-3">
            <Store aria-hidden="true" className="mt-0.5 size-5 shrink-0 text-primario" />
            <span>{textos.preguntamosA(solicitud.tiendasNotificadas, solicitud.producto)}</span>
          </li>
          <li className="flex items-start gap-3">
            <Clock aria-hidden="true" className="mt-0.5 size-5 shrink-0 text-primario" />
            <span>
              {textos.vence(formatearHora(solicitud.fechaVencimiento), minutosHasta(solicitud.fechaVencimiento))}
            </span>
          </li>
          <li className="flex items-start gap-3">
            <MapPin aria-hidden="true" className="mt-0.5 size-5 shrink-0 text-primario" />
            <span>{textos.radio(formatearDistancia(solicitud.radioEnMetros))}</span>
          </li>
        </ul>
      ) : (
        <EstadoVacio titulo={textos.sinTiendas(solicitud.producto)}>{textos.sugerenciaSinTiendas}</EstadoVacio>
      )}

      <Boton type="button" variante="secundario" className="self-start" onClick={alHacerOtra}>
        {textos.hacerOtraPregunta}
      </Boton>
    </section>
  )
}
