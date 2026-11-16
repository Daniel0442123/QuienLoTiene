import { TimerOff } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router'
import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { textosOrdenes } from '@/features/ordenes/textos'
import { formatearHora } from '@/services/formato'
import { useRespuestasDeSolicitud, type CriterioOrden } from './api'
import { TarjetaRespuesta } from './TarjetaRespuesta'
import { textosSolicitudes as textos } from './textos'

const CRITERIOS: CriterioOrden[] = ['precio', 'distancia', 'total']

/**
 * Respuestas de una solicitud para su cliente (HU-020) y lo que pasa cuando vence o ya se eligió (HU-021). Las
 * respuestas nuevas se anuncian por la región aria-live sin mover el foco (WCAG 4.1.3).
 */
export function RespuestasDeSolicitud({
  solicitudId,
  modalidad,
  fechaVencimiento,
  ordenId,
}: {
  solicitudId: string
  modalidad: 'RECOGER' | 'DOMICILIO'
  fechaVencimiento: string
  /** La orden creada al elegir (solicitud RESUELTA), para ir a ella. */
  ordenId?: string | null
}) {
  const anunciar = useAnunciar()
  const [orden, setOrden] = useState<CriterioOrden>('precio')
  const respuestas = useRespuestasDeSolicitud(solicitudId, orden)
  const datos = respuestas.data

  // Anuncia las que llegan después de la primera carga y el vencimiento (antes estaba abierta)
  const vistas = useRef<Set<string> | null>(null)
  const estadoAnterior = useRef<string | null>(null)
  useEffect(() => {
    if (!datos) return
    if (vistas.current) {
      for (const respuesta of datos.respuestas) {
        if (!vistas.current.has(respuesta.id)) anunciar(textos.llegoRespuesta(respuesta.nombreTienda))
      }
    }
    if (estadoAnterior.current === 'ABIERTA' && datos.estadoSolicitud === 'VENCIDA') anunciar(textos.vencioAnuncio)
    vistas.current = new Set(datos.respuestas.map((r) => r.id))
    estadoAnterior.current = datos.estadoSolicitud
  }, [datos, anunciar])

  if (respuestas.isPending) return <EsqueletoLista filas={2} etiqueta={textos.cargandoRespuestas} />
  if (respuestas.isError) {
    return <EstadoError error={respuestas.error} alReintentar={() => void respuestas.refetch()} />
  }

  const { estadoSolicitud, respuestasRecibidas, cantidadSolicitada } = respuestas.data
  const tarjetas = respuestas.data.respuestas
  const abierta = estadoSolicitud === 'ABIERTA'
  const elegida = tarjetas.find((r) => r.fueElegida)

  return (
    <section aria-labelledby="respuestas-titulo" className="flex flex-col gap-4">
      {estadoSolicitud === 'VENCIDA' ? (
        <div className="flex flex-col gap-2 rounded-control border border-advertencia p-4">
          <p className="flex items-center gap-2 font-semibold text-advertencia">
            <TimerOff aria-hidden="true" className="size-5 shrink-0" />
            {textos.estadoVencida}
          </p>
          <p>{textos.vencio(formatearHora(fechaVencimiento))}</p>
          <p className="text-texto-suave">
            {tarjetas.length === 0 ? textos.vencioSinRespuestas : textos.vencioConRespuestas}
          </p>
          <Boton asChild className="self-start">
            <Link to="/solicitudes/nueva">{textos.publicarDeNuevo}</Link>
          </Boton>
        </div>
      ) : null}

      {estadoSolicitud === 'RESUELTA' ? (
        <div className="flex flex-col gap-3 rounded-control border border-exito p-4">
          <p className="font-semibold text-exito">
            {elegida ? textos.yaElegiste(elegida.nombreTienda) : textos.yaElegisteSinTienda}
          </p>
          {ordenId ? (
            <Boton asChild className="self-start">
              <Link to={`/pedidos/${ordenId}`}>{textos.verMiPedido}</Link>
            </Boton>
          ) : null}
        </div>
      ) : null}

      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 id="respuestas-titulo" className="text-xl font-semibold">
          {textos.cantidadRespuestas(tarjetas.length)}
        </h2>
        {tarjetas.length > 1 ? (
          <fieldset className="flex flex-wrap items-center gap-2">
            <legend className="sr-only">{textos.ordenarPor}</legend>
            <span aria-hidden="true" className="text-texto-suave">
              {textos.ordenarPor}
            </span>
            {CRITERIOS.map((criterio) => (
              <label
                key={criterio}
                className="flex min-h-tactil cursor-pointer items-center gap-2 rounded-control border border-borde px-3 has-checked:border-primario has-checked:bg-superficie has-checked:font-semibold"
              >
                <input
                  type="radio"
                  name={`orden-${solicitudId}`}
                  value={criterio}
                  checked={orden === criterio}
                  onChange={() => setOrden(criterio)}
                  className="size-5 accent-primario"
                />
                {textos.criteriosOrden[criterio]}
              </label>
            ))}
          </fieldset>
        ) : null}
      </div>

      {tarjetas.length === 0 && abierta ? (
        <EstadoVacio titulo={textos.sinRespuestas}>
          {respuestasRecibidas > 0 ? textos.soloNo(respuestasRecibidas) : textos.sinRespuestasAyuda}
        </EstadoVacio>
      ) : (
        <ul className="flex flex-col gap-3" aria-busy={respuestas.isPlaceholderData || undefined}>
          {tarjetas.map((respuesta) => (
            <li key={respuesta.id}>
              <TarjetaRespuesta
                respuesta={respuesta}
                cantidadSolicitada={cantidadSolicitada}
                conDomicilio={modalidad === 'DOMICILIO'}
                accion={
                  abierta ? (
                    // HU-024: elegir lleva a confirmar el pedido; solo mientras la solicitud está abierta
                    <Boton asChild>
                      <Link
                        to={`/solicitudes/${solicitudId}/elegir/${respuesta.id}`}
                        aria-label={textosOrdenes.botonElegirDe(respuesta.nombreTienda)}
                      >
                        {textosOrdenes.botonElegir}
                      </Link>
                    </Boton>
                  ) : undefined
                }
              />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
