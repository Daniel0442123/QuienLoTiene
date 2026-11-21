import { useState } from 'react'
import { useNavigate } from 'react-router'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { useDirecciones } from '@/hooks/useDirecciones'
import { esErrorApi } from '@/services/errores'
import { mensajeParaUsuario } from '@/services/mensajes-error'
import { obtenerUbicacionActual } from '@/services/ubicacion'
import { useRepetirSolicitud, type SolicitudDelHistorial } from './api'
import { textosSolicitudes as textos } from './textos'

/**
 * HU-008, CP-CLI-008-01: «Repetir» publica otra igual con la ubicación de este momento (decisión 10 del contrato), no
 * con la de la solicitud original. Si el GPS no responde o no hay permiso, usa la primera dirección guardada y lo dice;
 * sin ninguna de las dos, explica cómo seguir. Al publicar abre «Esperando respuestas» de la nueva.
 * Cuenta para el límite de 10 por hora: SOL-001 dice cuánto esperar.
 */
export function BotonRepetir({ solicitud }: { solicitud: SolicitudDelHistorial }) {
  const navegar = useNavigate()
  const anunciar = useAnunciar()
  const direcciones = useDirecciones()
  const repetir = useRepetirSolicitud()
  const [buscando, setBuscando] = useState(false)
  const [sinUbicacion, setSinUbicacion] = useState(false)

  const alTocar = async () => {
    setSinUbicacion(false)
    repetir.reset()
    setBuscando(true)
    let ubicacion: { latitud: number; longitud: number } | null = null
    let aviso: string = textos.repetidaConGps
    try {
      ubicacion = await obtenerUbicacionActual()
    } catch {
      // Si las direcciones todavía no llegaron (tocó apenas abrió), se piden en ese momento
      const lista = direcciones.data ?? (await direcciones.refetch()).data
      const guardada = lista?.[0]
      if (guardada) {
        ubicacion = { latitud: guardada.latitud, longitud: guardada.longitud }
        aviso = textos.repetidaConDireccion(guardada.nombre)
      }
    } finally {
      setBuscando(false)
    }
    if (!ubicacion) {
      setSinUbicacion(true)
      return
    }
    repetir.mutate(
      { solicitudId: solicitud.id, ubicacion },
      {
        onSuccess: (nueva) => {
          anunciar(aviso)
          void navegar(`/solicitudes/nueva?solicitud=${nueva.id}`)
        },
      },
    )
  }

  const error = repetir.error
  const mensaje = sinUbicacion
    ? textos.sinUbicacionParaRepetir
    : error
      ? esErrorApi(error)
        ? mensajeParaUsuario(error)
        : textos.errorRepetir
      : null

  return (
    <div className="flex flex-col gap-1">
      {mensaje ? (
        <p role="alert" className="font-medium text-peligro">
          {mensaje}
        </p>
      ) : null}
      <Boton
        type="button"
        variante="secundario"
        ancho="completo"
        aria-label={textos.repetirDe(solicitud.producto)}
        enviando={buscando || repetir.isPending}
        textoEnviando={buscando ? textos.buscandoUbicacion : textos.repitiendo}
        onClick={() => void alTocar()}
      >
        {textos.botonRepetir}
      </Boton>
    </div>
  )
}
