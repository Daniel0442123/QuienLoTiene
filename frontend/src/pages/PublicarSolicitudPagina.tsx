import { useState } from 'react'
import { useAnunciar } from '@/components/RegionAnuncios'
import type { Solicitud } from '@/features/solicitudes/api'
import { EsperandoRespuestas } from '@/features/solicitudes/EsperandoRespuestas'
import { FormularioPublicarSolicitud } from '@/features/solicitudes/FormularioPublicarSolicitud'
import { textosSolicitudes as textos } from '@/features/solicitudes/textos'

/** /solicitudes/nueva · HU-016. El formulario y, al publicar, «Esperando respuestas». */
export function PublicarSolicitudPagina() {
  const anunciar = useAnunciar()
  const [publicada, setPublicada] = useState<Solicitud | null>(null)
  // Cambiar la clave reinicia el formulario vacío para hacer otra pregunta
  const [intento, setIntento] = useState(0)

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      {publicada ? (
        <EsperandoRespuestas
          solicitud={publicada}
          alHacerOtra={() => {
            setPublicada(null)
            setIntento((n) => n + 1)
          }}
        />
      ) : (
        <FormularioPublicarSolicitud
          key={intento}
          alPublicar={(solicitud) => {
            anunciar(textos.solicitudPublicada(solicitud.tiendasNotificadas))
            setPublicada(solicitud)
          }}
        />
      )}
    </div>
  )
}
