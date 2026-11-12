import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { useEffect, useRef } from 'react'
import { Boton } from './ui/Boton'

export interface Coordenadas {
  latitud: number
  longitud: number
}

/** Centro de Popayán: donde arranca el mapa si todavía no hay ubicación. */
const CENTRO_INICIAL: Coordenadas = { latitud: 2.4419, longitud: -76.6063 }
const ZOOM_INICIAL = 16

const textos = {
  etiquetaMapa: 'Mapa. Con las flechas mueves el mapa y con + y − cambias el acercamiento.',
  marcarCentro: 'Marcar el centro del mapa',
  atribucion: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>',
}

/** Pin dibujado con los tokens (sin imágenes de Leaflet, que Vite no resuelve bien). */
const iconoPin = L.divIcon({
  className: '',
  html: '<span class="block size-7 -translate-y-1/2 rounded-full rounded-br-none rotate-45 border-2 border-fondo bg-primario shadow-md"></span>',
  iconSize: [28, 28],
  iconAnchor: [14, 28],
})

/**
 * Mapa de OpenStreetMap con Leaflet directo (Tabla 18: sin react-leaflet ni Google Maps). Se carga con `lazy` solo en
 * las pantallas que lo usan. Para no depender de arrastrar (WCAG 2.5.7): un toque pone el pin, y con el teclado se
 * mueve el mapa con las flechas y se marca el centro con el botón.
 */
export default function MapaUbicacion({
  valor,
  alCambiar,
}: {
  valor: Coordenadas | null
  alCambiar: (coordenadas: Coordenadas) => void
}) {
  const contenedor = useRef<HTMLDivElement>(null)
  const mapa = useRef<L.Map | null>(null)
  const pin = useRef<L.Marker | null>(null)
  const alCambiarActual = useRef(alCambiar)

  useEffect(() => {
    alCambiarActual.current = alCambiar
  }, [alCambiar])

  // Crear el mapa una sola vez
  useEffect(() => {
    if (!contenedor.current) return
    const inicio = valor ?? CENTRO_INICIAL
    const instancia = L.map(contenedor.current, {
      center: [inicio.latitud, inicio.longitud],
      zoom: ZOOM_INICIAL,
      scrollWheelZoom: false, // la rueda debe seguir desplazando la página
    })
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: textos.atribucion,
    }).addTo(instancia)
    instancia.attributionControl.setPrefix(false)
    instancia.on('click', (evento: L.LeafletMouseEvent) =>
      alCambiarActual.current({ latitud: evento.latlng.lat, longitud: evento.latlng.lng }),
    )
    mapa.current = instancia
    return () => {
      instancia.remove()
      mapa.current = null
      pin.current = null
    }
    // Solo al montar: los cambios de `valor` se aplican en el efecto siguiente
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Mover el pin (y el mapa) cuando cambia la ubicación desde afuera, por ejemplo con «Usar mi ubicación»
  useEffect(() => {
    const instancia = mapa.current
    if (!instancia || !valor) return
    const punto = L.latLng(valor.latitud, valor.longitud)
    if (pin.current) {
      pin.current.setLatLng(punto)
    } else {
      pin.current = L.marker(punto, { icon: iconoPin, draggable: true, keyboard: false }).addTo(instancia)
      pin.current.on('dragend', () => {
        const posicion = pin.current?.getLatLng()
        if (posicion) alCambiarActual.current({ latitud: posicion.lat, longitud: posicion.lng })
      })
    }
    if (!instancia.getBounds().contains(punto)) instancia.setView(punto)
  }, [valor])

  return (
    <div className="flex flex-col gap-2">
      <div
        ref={contenedor}
        role="region"
        aria-label={textos.etiquetaMapa}
        className="z-0 h-64 w-full rounded-control border border-borde"
      />
      <Boton
        type="button"
        variante="secundario"
        className="self-start"
        onClick={() => {
          const centro = mapa.current?.getCenter()
          if (centro) alCambiarActual.current({ latitud: centro.lat, longitud: centro.lng })
        }}
      >
        {textos.marcarCentro}
      </Boton>
    </div>
  )
}
