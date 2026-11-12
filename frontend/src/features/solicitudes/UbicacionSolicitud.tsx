import { MapPin } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { EstadoError } from '@/components/Estados'
import { SelectorUbicacion, type Coordenadas } from '@/components/SelectorUbicacion'
import { useDirecciones } from '@/hooks/useDirecciones'
import { textosSolicitudes as textos } from './textos'

type Eleccion = { tipo: 'direccion'; id: string } | { tipo: 'otra' }

/**
 * Cerca de dónde se busca (HU-016). Para no pedir dos veces el mismo dato (WCAG 3.3.7) y publicar en ≤ 60 s
 * (RNF-027), la primera dirección guardada viene marcada; «Otra ubicación» abre el GPS y el mapa.
 */
export function UbicacionSolicitud({
  id,
  valor,
  alCambiar,
  error,
}: {
  id: string
  valor: Coordenadas | null
  alCambiar: (coordenadas: Coordenadas) => void
  error?: string
}) {
  const direcciones = useDirecciones()
  const [eleccionDeLaPersona, setEleccion] = useState<Eleccion | null>(null)
  const primera = direcciones.data?.[0]
  // Mientras la persona no elija, vale la primera dirección guardada
  const eleccion: Eleccion | null = eleccionDeLaPersona ?? (primera ? { tipo: 'direccion', id: primera.id } : null)

  // Al llegar las direcciones, la primera pasa al formulario si todavía no hay ubicación
  useEffect(() => {
    if (eleccionDeLaPersona === null && valor === null && primera) {
      alCambiar({ latitud: primera.latitud, longitud: primera.longitud })
    }
  }, [eleccionDeLaPersona, valor, primera, alCambiar])

  const idError = `${id}-error`
  const sinDirecciones = direcciones.isSuccess && direcciones.data.length === 0
  const mostrarMapa = eleccion?.tipo === 'otra' || sinDirecciones || direcciones.isError

  return (
    <fieldset id={id} className="flex flex-col gap-2" aria-describedby={error ? idError : undefined}>
      <legend className="mb-1 font-semibold">{textos.etiquetaUbicacion}</legend>

      {direcciones.isPending ? (
        <div
          role="status"
          aria-label={textos.cargandoDirecciones}
          className="h-tactil animate-pulse rounded-control bg-borde-suave"
        />
      ) : direcciones.isError ? (
        <EstadoError error={direcciones.error} alReintentar={() => void direcciones.refetch()} />
      ) : sinDirecciones ? (
        <p className="text-sm text-texto-suave">
          {textos.sinDirecciones}{' '}
          <Link to="/cuenta" className="text-primario underline underline-offset-4 hover:no-underline">
            {textos.enlaceGuardarDirecciones}
          </Link>
        </p>
      ) : (
        <div className="flex flex-col gap-2">
          {direcciones.data.map((direccion) => (
            <label
              key={direccion.id}
              className="flex min-h-tactil cursor-pointer items-center gap-3 rounded-control border border-borde px-3 py-2 has-checked:border-primario has-checked:bg-superficie"
            >
              <input
                type="radio"
                name="ubicacion-solicitud"
                className="size-5 shrink-0 accent-primario"
                checked={eleccion?.tipo === 'direccion' && eleccion.id === direccion.id}
                onChange={() => {
                  setEleccion({ tipo: 'direccion', id: direccion.id })
                  alCambiar({ latitud: direccion.latitud, longitud: direccion.longitud })
                }}
              />
              <span className="flex flex-col font-medium">
                {direccion.nombre}
                <span className="text-sm font-normal break-words text-texto-suave">{direccion.direccionEscrita}</span>
              </span>
            </label>
          ))}
          <label className="flex min-h-tactil cursor-pointer items-center gap-3 rounded-control border border-borde px-3 has-checked:border-primario has-checked:bg-superficie">
            <input
              type="radio"
              name="ubicacion-solicitud"
              className="size-5 shrink-0 accent-primario"
              checked={eleccion?.tipo === 'otra'}
              onChange={() => setEleccion({ tipo: 'otra' })}
            />
            <MapPin aria-hidden="true" className="size-5 shrink-0 text-texto-suave" />
            {textos.otraUbicacion}
          </label>
        </div>
      )}

      {direcciones.isError ? <p className="text-sm text-texto-suave">{textos.errorDirecciones}</p> : null}

      {mostrarMapa ? (
        <SelectorUbicacion
          etiqueta={textos.etiquetaMapa}
          pista={textos.pistaMapa}
          // Al pasar a «Otra ubicación» el pin arranca en la dirección que estaba elegida: se mueve desde ahí
          valor={valor}
          alCambiar={alCambiar}
        />
      ) : null}

      {error ? (
        <p id={idError} className="text-sm font-medium text-peligro">
          {error}
        </p>
      ) : null}
    </fieldset>
  )
}
