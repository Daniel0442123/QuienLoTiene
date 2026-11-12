import { lazy, Suspense, useId, useState } from 'react'
import { MapPin } from 'lucide-react'
import type { Coordenadas } from './MapaUbicacion'
import { Boton } from './ui/Boton'
import { cn } from './ui/utilidades'

// Leaflet pesa: se descarga solo cuando una pantalla muestra este selector (F-15, F-16)
const MapaUbicacion = lazy(() => import('./MapaUbicacion'))

export type { Coordenadas }

const textos = {
  usarMiUbicacion: 'Usar mi ubicación',
  buscando: 'Buscando tu ubicación…',
  ubicacionMarcada: 'Ubicación marcada en el mapa.',
  sinUbicacion: 'Todavía no has marcado la ubicación.',
  permisoNegado:
    'No pudimos usar tu ubicación porque el permiso está bloqueado. Marca el lugar tocando el mapa o actívalo en el navegador.',
  sinGps: 'No pudimos encontrar tu ubicación. Marca el lugar tocando el mapa.',
  cargandoMapa: 'Cargando el mapa…',
}

type EstadoGps = 'quieto' | 'buscando' | 'negado' | 'fallo'

/**
 * Ubicación con dos caminos que no exigen arrastrar (WCAG 2.5.7): el GPS del celular con «Usar mi ubicación», o un
 * toque en el mapa. El resultado se anuncia como texto, no solo con el pin.
 */
export function SelectorUbicacion({
  etiqueta,
  pista,
  valor,
  alCambiar,
  error,
  id,
}: {
  etiqueta: string
  pista?: string
  valor: Coordenadas | null
  alCambiar: (coordenadas: Coordenadas) => void
  error?: string
  /** id del botón principal: el resumen de errores lleva el foco ahí. */
  id?: string
}) {
  const idGenerado = useId()
  const idBoton = id ?? idGenerado
  const idPista = `${idBoton}-pista`
  const idEstado = `${idBoton}-estado`
  const idError = `${idBoton}-error`
  const [estadoGps, setEstadoGps] = useState<EstadoGps>('quieto')

  const usarMiUbicacion = () => {
    if (!('geolocation' in navigator)) {
      setEstadoGps('fallo')
      return
    }
    setEstadoGps('buscando')
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        setEstadoGps('quieto')
        alCambiar({ latitud: coords.latitude, longitud: coords.longitude })
      },
      (falla) => setEstadoGps(falla.code === falla.PERMISSION_DENIED ? 'negado' : 'fallo'),
      { enableHighAccuracy: true, timeout: 15_000, maximumAge: 60_000 },
    )
  }

  const aviso = estadoGps === 'negado' ? textos.permisoNegado : estadoGps === 'fallo' ? textos.sinGps : null

  return (
    <fieldset
      className={cn('flex flex-col gap-3 rounded-control border border-borde p-3', error && 'border-peligro')}
      aria-describedby={[pista ? idPista : null, error ? idError : null].filter(Boolean).join(' ') || undefined}
    >
      <legend className="px-1 font-medium">{etiqueta}</legend>
      {pista ? (
        <p id={idPista} className="text-sm text-texto-suave">
          {pista}
        </p>
      ) : null}

      <Boton
        id={idBoton}
        type="button"
        variante="secundario"
        className="self-start"
        enviando={estadoGps === 'buscando'}
        textoEnviando={textos.buscando}
        onClick={usarMiUbicacion}
      >
        <MapPin aria-hidden="true" className="size-5" />
        {textos.usarMiUbicacion}
      </Boton>

      {aviso ? <p className="text-sm font-medium text-advertencia">{aviso}</p> : null}

      <Suspense
        fallback={
          <div
            className="h-64 animate-pulse rounded-control bg-borde-suave"
            aria-label={textos.cargandoMapa}
            role="status"
          />
        }
      >
        <MapaUbicacion valor={valor} alCambiar={alCambiar} />
      </Suspense>

      <p id={idEstado} role="status" className={cn('text-sm', valor ? 'font-medium text-exito' : 'text-texto-suave')}>
        {valor ? textos.ubicacionMarcada : textos.sinUbicacion}
      </p>

      {error ? (
        <p id={idError} className="text-sm font-medium text-peligro">
          {error}
        </p>
      ) : null}
    </fieldset>
  )
}
