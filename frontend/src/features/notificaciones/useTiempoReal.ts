import { useQueryClient, type QueryClient } from '@tanstack/react-query'
import { useEffect, useRef } from 'react'
import { useAnunciar } from '@/components/RegionAnuncios'
import { clavesOrdenes } from '@/features/ordenes/api'
import { clavesSolicitudesTienda } from '@/features/solicitudes/api'
import { useSesion } from '@/hooks/useSesion'
import { sonarAlertaUrgente } from '@/services/alertaSonora'
import { escucharEventos } from '@/services/eventos'
import { textosNotificaciones as textos } from './textos'
import type { components } from '@/services/api-tipos'

type Esquemas = components['schemas']

type Anunciar = ReturnType<typeof useAnunciar>

/**
 * Qué hace cada evento (contrato del Sprint 2): los eventos traen solo ids y datos mínimos, así que se invalida la
 * consulta de TanStack Query que corresponda y la pantalla pide el detalle por REST. Los avisos en texto van por la
 * región aria-live (WCAG 4.1.3). Las respuestas nuevas y los cambios de estado ya los anuncia su propia pantalla.
 */
export function manejarEvento(tipo: string, datos: unknown, consultas: QueryClient, anunciar: Anunciar): void {
  const invalidar = (queryKey: readonly unknown[]) => void consultas.invalidateQueries({ queryKey })
  switch (tipo) {
    case 'solicitud-nueva': {
      const evento = datos as Esquemas['EventoSolicitudNueva']
      invalidar(clavesSolicitudesTienda.disponibles)
      if (evento.esUrgente) {
        sonarAlertaUrgente()
        anunciar(textos.solicitudUrgente(evento.producto))
      } else {
        anunciar(textos.solicitudNueva(evento.producto))
      }
      return
    }
    case 'respuesta-recibida':
    case 'solicitud-vencida': {
      const evento = datos as Esquemas['EventoRespuestaRecibida'] | Esquemas['EventoSolicitudVencida']
      invalidar(['solicitudes', evento.solicitudId])
      return
    }
    case 'solicitud-cerrada':
    case 'respuesta-no-elegida':
      invalidar(clavesSolicitudesTienda.disponibles)
      invalidar(clavesSolicitudesTienda.misRespuestas)
      return
    case 'orden-creada': {
      const evento = datos as Esquemas['EventoOrdenCreada']
      invalidar(clavesOrdenes.todas)
      invalidar(clavesSolicitudesTienda.misRespuestas)
      anunciar(textos.pedidoNuevo(evento.producto))
      return
    }
    case 'estado-orden-cambiado':
      // El prefijo ['ordenes'] refresca la lista y el detalle de esa orden
      invalidar(clavesOrdenes.todas)
      return
    default:
    // Un tipo nuevo del backend no rompe nada: se ignora hasta que el frontend lo conozca
  }
}

/** Abre el flujo de eventos mientras haya sesión y lo cierra al salir (HU-030). */
export function useTiempoReal(): void {
  const sesion = useSesion()
  const consultas = useQueryClient()
  const anunciar = useAnunciar()
  const usuarioId = sesion?.usuarioId
  const manejar = useRef(manejarEvento)

  useEffect(() => {
    if (!usuarioId) return
    const control = new AbortController()
    void escucharEventos({
      senal: control.signal,
      alEvento: (tipo, datos) => manejar.current(tipo, datos, consultas, anunciar),
    })
    return () => control.abort()
  }, [usuarioId, consultas, anunciar])
}
