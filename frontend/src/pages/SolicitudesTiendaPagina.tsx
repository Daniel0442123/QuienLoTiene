import { useCallback, useEffect, useState } from 'react'
import { AvisoDeshacer } from '@/components/AvisoDeshacer'
import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import { useAnunciar } from '@/components/RegionAnuncios'
import { useResponderSolicitud, useSolicitudesDisponibles, type SolicitudDisponible } from '@/features/solicitudes/api'
import { PestanasSolicitudesTienda } from '@/features/solicitudes/PestanasSolicitudesTienda'
import { idTarjetaSolicitud } from '@/features/solicitudes/presentacion'
import { TarjetaSolicitudDisponible } from '@/features/solicitudes/TarjetaSolicitudDisponible'
import { textosSolicitudes as textos } from '@/features/solicitudes/textos'
import { EncabezadoSeccionTienda } from '@/features/tiendas/EncabezadoSeccionTienda'
import { useAhora } from '@/hooks/useAhora'
import { useDescartarConDeshacer } from '@/hooks/useDescartarConDeshacer'

/**
 * /tienda/solicitudes · HU-018: solicitudes nuevas de la tienda, urgentes primero. «No lo tengo» en 1 toque con
 * deshacer (CP-SOL-019-03); «Sí lo tengo» y «Tengo algo parecido» abren la pantalla para responder (HU-019).
 */
export function SolicitudesTiendaPagina() {
  const anunciar = useAnunciar()
  const disponibles = useSolicitudesDisponibles()
  const ahora = useAhora()
  const { mutate: responder } = useResponderSolicitud()
  // Las que ya se enviaron como NO se ocultan hasta que la lista se vuelva a pedir sin ellas
  const [enviadas, setEnviadas] = useState<string[]>([])
  const [restaurada, setRestaurada] = useState<string | null>(null)

  const enviarNo = useCallback(
    (solicitud: SolicitudDisponible) => {
      setEnviadas((anteriores) => [...anteriores, solicitud.id])
      responder(
        { solicitudId: solicitud.id, tipo: 'NO' },
        {
          onError: () => {
            setEnviadas((anteriores) => anteriores.filter((id) => id !== solicitud.id))
            anunciar(textos.errorDescartar(solicitud.producto), 'urgente')
          },
        },
      )
    },
    [responder, anunciar],
  )
  const { pendiente, descartar, deshacer, confirmar } = useDescartarConDeshacer(enviarNo)

  // Al deshacer, el foco vuelve a la tarjeta que reapareció
  useEffect(() => {
    if (restaurada) document.getElementById(idTarjetaSolicitud(restaurada))?.focus()
  }, [restaurada])

  const visibles = (disponibles.data ?? []).filter(
    (solicitud) => solicitud.id !== pendiente?.id && !enviadas.includes(solicitud.id),
  )

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <EncabezadoSeccionTienda
        titulo={textos.tituloSolicitudesTienda}
        descripcion={textos.descripcionSolicitudesTienda}
      />
      <PestanasSolicitudesTienda />

      {disponibles.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargandoSolicitudes} />
      ) : disponibles.isError ? (
        <EstadoError error={disponibles.error} alReintentar={() => void disponibles.refetch()} />
      ) : (
        <div className="flex flex-col gap-3">
          <p className="font-semibold">{textos.esperandoRespuesta(visibles.length)}</p>
          {visibles.length === 0 ? (
            <EstadoVacio titulo={textos.sinSolicitudes}>{textos.sinSolicitudesAyuda}</EstadoVacio>
          ) : (
            <ul className="flex flex-col gap-3">
              {visibles.map((solicitud) => (
                <li key={solicitud.id}>
                  <TarjetaSolicitudDisponible
                    solicitud={solicitud}
                    ahora={ahora}
                    alDescartar={(descartada) => {
                      setRestaurada(null)
                      descartar(descartada)
                      anunciar(textos.descartasteAnuncio(descartada.producto))
                    }}
                  />
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {pendiente ? (
        <AvisoDeshacer
          mensaje={textos.descartaste(pendiente.producto)}
          alDeshacer={() => {
            const devuelta = deshacer()
            if (devuelta) {
              setRestaurada(devuelta.id)
              anunciar(textos.restaurada(devuelta.producto))
            }
          }}
          alCerrar={confirmar}
        />
      ) : null}
    </section>
  )
}
