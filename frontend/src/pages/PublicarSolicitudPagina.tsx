import { useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { useAnunciar } from '@/components/RegionAnuncios'
import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { clavesSolicitudCliente, useSolicitud } from '@/features/solicitudes/api'
import { EsperandoRespuestas } from '@/features/solicitudes/EsperandoRespuestas'
import { FormularioPublicarSolicitud } from '@/features/solicitudes/FormularioPublicarSolicitud'
import { textosSolicitudes as textos } from '@/features/solicitudes/textos'

/** Parámetro de la URL con la solicitud publicada: así sobrevive a recargar la página. */
const PARAMETRO_SOLICITUD = 'solicitud'

/**
 * /solicitudes/nueva · HU-016. El formulario y, al publicar, «Esperando respuestas».
 *
 * La solicitud publicada queda en la URL (`?solicitud=<id>`). Si el cliente recarga la página, la app la vuelve a
 * pedir a la API y muestra la misma pantalla con sus respuestas; antes volvía al formulario vacío y el cliente perdía
 * las respuestas de las tiendas.
 */
export function PublicarSolicitudPagina() {
  const anunciar = useAnunciar()
  const clienteConsultas = useQueryClient()
  const [parametros, setParametros] = useSearchParams()
  const solicitudId = parametros.get(PARAMETRO_SOLICITUD)
  // Cambiar la clave reinicia el formulario vacío para hacer otra pregunta
  const [intento, setIntento] = useState(0)

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      {solicitudId ? (
        <SolicitudPublicada
          solicitudId={solicitudId}
          alHacerOtra={() => {
            setParametros({}, { replace: true })
            setIntento((n) => n + 1)
          }}
        />
      ) : (
        <FormularioPublicarSolicitud
          key={intento}
          alPublicar={(solicitud) => {
            anunciar(textos.solicitudPublicada(solicitud.tiendasNotificadas))
            // La respuesta de publicar ya trae la solicitud: se muestra sin volver a pedirla
            clienteConsultas.setQueryData(clavesSolicitudCliente.solicitud(solicitud.id), solicitud)
            setParametros({ [PARAMETRO_SOLICITUD]: solicitud.id }, { replace: true })
          }}
        />
      )}
    </div>
  )
}

function SolicitudPublicada({ solicitudId, alHacerOtra }: { solicitudId: string; alHacerOtra: () => void }) {
  const solicitud = useSolicitud(solicitudId)

  if (solicitud.isPending) return <EsqueletoLista filas={2} etiqueta={textos.cargandoRespuestas} />
  if (solicitud.isError) return <EstadoError error={solicitud.error} alReintentar={() => void solicitud.refetch()} />
  return <EsperandoRespuestas solicitud={solicitud.data} alHacerOtra={alHacerOtra} />
}
