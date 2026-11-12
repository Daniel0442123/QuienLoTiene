import { DoorClosed, DoorOpen } from 'lucide-react'
import { useId } from 'react'
import { useAnunciar } from '@/components/RegionAnuncios'
import { cn } from '@/components/ui/utilidades'
import { esErrorApi } from '@/services/errores'
import { mensajeParaUsuario } from '@/services/mensajes-error'
import { useCambiarApertura, type MiTienda } from './api'
import { textosTiendas as textos } from './textos'

/**
 * Abrir o cerrar la tienda (HU-013, CU-19) con un toque. Patrón «switch» del APG: botón con role="switch" y
 * aria-checked; el estado se dice con texto e ícono, no solo con color. Solo una tienda APROBADA puede cambiarlo:
 * si no, queda deshabilitado con la razón visible.
 */
export function InterruptorAperturaTienda({ tienda }: { tienda: MiTienda }) {
  const anunciar = useAnunciar()
  const apertura = useCambiarApertura()
  const idEtiqueta = useId()
  const idExplicacion = useId()
  const idError = useId()

  const aprobada = tienda.estado === 'APROBADA'
  const abierta = tienda.estaAbierta
  const error = apertura.error
  const mensajeError = error ? (esErrorApi(error) ? mensajeParaUsuario(error) : textos.errorApertura) : null
  const explicacion = !aprobada
    ? textos.razonNoAprobada
    : abierta
      ? textos.explicacionAbierta
      : textos.explicacionCerrada

  return (
    <div className="flex flex-col gap-2 rounded-control border border-borde p-4">
      <div className="flex items-center justify-between gap-4">
        <span id={idEtiqueta} className="font-semibold">
          {textos.etiquetaApertura}
        </span>
        <button
          type="button"
          role="switch"
          aria-checked={abierta}
          aria-labelledby={idEtiqueta}
          aria-describedby={[idExplicacion, mensajeError ? idError : null].filter(Boolean).join(' ')}
          disabled={!aprobada || apertura.isPending}
          onClick={() =>
            apertura.mutate(!abierta, {
              onSuccess: (actualizada) =>
                anunciar(actualizada.estaAbierta ? textos.tiendaAbierta : textos.tiendaCerrada),
            })
          }
          className={cn(
            'inline-flex min-h-tactil min-w-32 items-center justify-center gap-2 rounded-full border-2 px-4 font-semibold transition-colors',
            'disabled:cursor-not-allowed disabled:opacity-60',
            abierta ? 'border-exito bg-exito text-primario-texto' : 'border-borde bg-fondo text-texto',
          )}
        >
          {abierta ? (
            <DoorOpen aria-hidden="true" className="size-5" />
          ) : (
            <DoorClosed aria-hidden="true" className="size-5" />
          )}
          {abierta ? textos.abierta : textos.cerrada}
        </button>
      </div>
      <p id={idExplicacion} className="text-sm text-texto-suave">
        {explicacion}
      </p>
      {mensajeError ? (
        <p id={idError} role="alert" className="text-sm font-medium text-peligro">
          {mensajeError}
        </p>
      ) : null}
    </div>
  )
}
