import { Bell, BellOff } from 'lucide-react'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { esErrorApi } from '@/services/errores'
import { mensajeParaUsuario } from '@/services/mensajes-error'
import { useActivarPush, useConfiguracionPush, useDesactivarPush, useEstadoPush } from './api'
import { textosNotificaciones as textos } from './textos'

/**
 * HU-031 en el panel de la tienda: activar o desactivar los avisos push de este dispositivo. No aparece si el
 * servidor tiene el push apagado (`habilitado: false`), y el permiso del navegador se pide solo al tocar «Activar
 * avisos». Si no se puede (navegador sin push, permiso bloqueado), se dice por qué y que las solicitudes igual esperan
 * en el panel (CP-NOT-031-02).
 */
export function TarjetaAvisosPush() {
  const anunciar = useAnunciar()
  const configuracion = useConfiguracionPush()
  const habilitado = configuracion.data?.habilitado === true && Boolean(configuracion.data.clavePublica)
  const estado = useEstadoPush(habilitado)
  const activar = useActivarPush()
  const desactivar = useDesactivarPush()

  if (!habilitado || !estado.data) return null
  const { estado: actual, suscripcion } = estado.data
  const error = activar.error ?? desactivar.error
  const mensajeError = error
    ? esErrorApi(error)
      ? mensajeParaUsuario(error)
      : activar.error
        ? textos.errorActivar
        : textos.errorDesactivar
    : null

  const explicacion = {
    'no-soportado': textos.noSoportado,
    'sin-service-worker': textos.sinServiceWorker,
    bloqueado: textos.bloqueado,
    activo: textos.estadoActivo,
    inactivo: textos.estadoInactivo,
  }[actual]

  return (
    <section aria-labelledby="avisos-push" className="flex flex-col gap-3 rounded-control border border-borde p-4">
      <h2 id="avisos-push" className="flex items-center gap-2 text-lg font-semibold">
        {actual === 'activo' ? (
          <Bell aria-hidden="true" className="size-5 shrink-0 text-primario" />
        ) : (
          <BellOff aria-hidden="true" className="size-5 shrink-0 text-texto-suave" />
        )}
        {textos.tituloAvisos}
      </h2>
      <p className="text-texto-suave">{textos.explicacionAvisos}</p>
      <p className="font-medium">{explicacion}</p>
      {mensajeError ? (
        <p role="alert" className="font-medium text-peligro">
          {mensajeError}
        </p>
      ) : null}
      {actual === 'inactivo' ? (
        <Boton
          type="button"
          className="self-start"
          enviando={activar.isPending}
          textoEnviando={textos.activando}
          onClick={() =>
            activar.mutate(configuracion.data?.clavePublica ?? '', {
              onSuccess: (resultado) => {
                if (resultado === 'activo') anunciar(textos.avisosActivados)
              },
            })
          }
        >
          {textos.botonActivar}
        </Boton>
      ) : null}
      {actual === 'activo' && suscripcion ? (
        <Boton
          type="button"
          variante="secundario"
          className="self-start"
          enviando={desactivar.isPending}
          textoEnviando={textos.desactivando}
          onClick={() => desactivar.mutate(suscripcion, { onSuccess: () => anunciar(textos.avisosDesactivados) })}
        >
          {textos.botonDesactivar}
        </Boton>
      ) : null}
    </section>
  )
}
