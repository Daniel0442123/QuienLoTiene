import { Clock, MapPin, Store } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { EstadoVacio } from '@/components/Estados'
import { Boton } from '@/components/ui/Boton'
import { useAhora } from '@/hooks/useAhora'
import { formatearDistancia, formatearHora, minutosHasta } from '@/services/formato'
import { useSolicitud, type Solicitud } from './api'
import { AvisoRestringido } from './AvisoRestringido'
import { RespuestasDeSolicitud } from './RespuestasDeSolicitud'
import { textosSolicitudes as textos } from './textos'

/**
 * Después de publicar (paso 9 de la secuencia: «Esperando respuestas»). Dice a cuántas tiendas se les preguntó, hasta
 * cuándo pueden responder y a qué distancia; si no hubo ninguna, lo dice y sugiere qué hacer (estado vacío).
 * La pantalla cambia sin cambiar de ruta: el foco va al título para que el lector de pantalla lo lea. Debajo llegan
 * las respuestas de las tiendas (HU-020).
 *
 * DEF-008: el encabezado sigue el estado actual de la solicitud, no el de la publicación. Al vencer dice que venció y
 * ya no que las tiendas pueden responder; los minutos que faltan se recalculan con el reloj.
 */
export function EsperandoRespuestas({ solicitud, alHacerOtra }: { solicitud: Solicitud; alHacerOtra: () => void }) {
  const titulo = useRef<HTMLHeadingElement>(null)
  const ahora = useAhora()
  const actual = useSolicitud(solicitud.id).data ?? solicitud
  const estado = actual.estado
  // Pasada la hora ya no se puede responder, aunque la API todavía no haya marcado el vencimiento
  const vencida =
    estado === 'VENCIDA' || (estado === 'ABIERTA' && Date.parse(solicitud.fechaVencimiento) <= ahora.getTime())
  const textoTitulo = vencida
    ? textos.tituloVencida
    : estado === 'ABIERTA'
      ? textos.tituloEsperando
      : textos.tituloRespuestas

  useEffect(() => {
    document.title = `${textoTitulo} · ¿Quién lo tiene?`
  }, [textoTitulo])
  useEffect(() => {
    titulo.current?.focus()
  }, [])

  const hayTiendas = solicitud.tiendasNotificadas > 0
  const hora = formatearHora(solicitud.fechaVencimiento)

  return (
    <section className="flex flex-col gap-5">
      <h1 ref={titulo} tabIndex={-1} className="text-2xl font-bold">
        {textoTitulo}
      </h1>

      {/* HU-023: la marca que guardó el backend al publicar */}
      {solicitud.esRestringida ? <AvisoRestringido /> : null}

      {hayTiendas ? (
        <ul className="flex flex-col gap-3">
          <li className="flex items-start gap-3">
            <Store aria-hidden="true" className="mt-0.5 size-5 shrink-0 text-primario" />
            <span>{textos.preguntamosA(solicitud.tiendasNotificadas, solicitud.producto)}</span>
          </li>
          {estado === 'RESUELTA' ? null : (
            <li className="flex items-start gap-3">
              <Clock aria-hidden="true" className="mt-0.5 size-5 shrink-0 text-primario" />
              <span>
                {vencida
                  ? textos.plazoTerminado(hora)
                  : textos.vence(hora, minutosHasta(solicitud.fechaVencimiento, ahora))}
              </span>
            </li>
          )}
          <li className="flex items-start gap-3">
            <MapPin aria-hidden="true" className="mt-0.5 size-5 shrink-0 text-primario" />
            <span>{textos.radio(formatearDistancia(solicitud.radioEnMetros))}</span>
          </li>
        </ul>
      ) : (
        <EstadoVacio titulo={textos.sinTiendas(solicitud.producto)}>{textos.sugerenciaSinTiendas}</EstadoVacio>
      )}

      {hayTiendas ? (
        <RespuestasDeSolicitud
          solicitudId={solicitud.id}
          modalidad={solicitud.modalidad}
          fechaVencimiento={solicitud.fechaVencimiento}
          ordenId={actual.ordenId}
        />
      ) : null}

      <Boton type="button" variante="secundario" className="self-start" onClick={alHacerOtra}>
        {textos.hacerOtraPregunta}
      </Boton>
    </section>
  )
}
