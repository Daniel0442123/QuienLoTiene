import { zodResolver } from '@hookform/resolvers/zod'
import { useId, useRef, useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { Link } from 'react-router'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { cn } from '@/components/ui/utilidades'
import { esErrorApi } from '@/services/errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { useCancelarOrden, type Orden } from './api'
import { esquemaCancelar, LARGO_MAXIMO_MOTIVO, type DatosCancelar } from './esquemas'
import { puedeCancelar, type VistaOrden } from './presentacion'
import { textosOrdenes as textos } from './textos'

/**
 * HU-029: cancelar el pedido con motivo y confirmación (DECISIONES-FRONTEND §2: «confirmar antes de cancelar una
 * orden»). El botón abre el formulario en el mismo lugar, sin diálogo, como el rechazo de tiendas: el foco va al
 * motivo y «No, volver» lo cierra sin cambiar nada. Si la orden ya salió (ORD-005) o cambió en otro celular, el
 * botón deja de ofrecerse pero el mensaje se queda para que la persona sepa qué pasó y cómo seguir.
 */
export function CancelarPedido({ orden, vista }: { orden: Orden; vista: VistaOrden }) {
  const anunciar = useAnunciar()
  const cancelacion = useCancelarOrden(orden.id)
  const [abierto, setAbierto] = useState(false)
  const botonCancelar = useRef<HTMLButtonElement>(null)
  const idMotivo = useId()
  const {
    register,
    control,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<DatosCancelar>({
    resolver: zodResolver(esquemaCancelar),
    mode: 'onTouched',
    defaultValues: { motivo: '' },
  })
  const largo = useWatch({ control, name: 'motivo' }).length

  const error = cancelacion.error
  // Un error de un campo se muestra en el campo; los demás (ORD-005, ORD-002, sin conexión) arriba de los botones
  const errorGeneral = error && !(esErrorApi(error) && error.esErrorDeValidacion) ? error : null
  const mensajeError = errorGeneral
    ? esErrorApi(errorGeneral)
      ? mensajeParaUsuario(errorGeneral)
      : textos.errorCancelar
    : null
  const alertaError = mensajeError ? (
    <div role="alert" className="rounded-control border border-peligro p-3 font-medium text-peligro">
      <p>{mensajeError}</p>
      {/* ORD-005: ya salió; el camino es reportar (HU-035) o escribir a la tienda (WhatsApp, más arriba) */}
      {esErrorApi(errorGeneral) && errorGeneral.codigo === 'ORD-005' ? (
        <Link
          to={vista === 'cliente' ? `/pedidos/${orden.id}/reportar` : `/tienda/pedidos/${orden.id}/reportar`}
          className="mt-2 inline-flex min-h-tactil items-center font-semibold text-primario underline underline-offset-4 hover:no-underline"
        >
          {textos.enlaceReportarProblema}
        </Link>
      ) : null}
      {esErrorApi(errorGeneral) && debeMostrarIdCorrelacion(errorGeneral) ? (
        <p className="mt-1 text-sm font-normal text-texto-suave">
          {textos.codigoSoporteOrden(errorGeneral.idCorrelacion)}
        </p>
      ) : null}
    </div>
  ) : null

  if (!puedeCancelar(vista, orden.estado)) return alertaError

  if (!abierto) {
    return (
      <div className="flex flex-col gap-2">
        {alertaError}
        <Boton
          ref={botonCancelar}
          type="button"
          variante="secundario"
          ancho="completo"
          className="border-peligro text-peligro"
          onClick={() => {
            cancelacion.reset()
            setAbierto(true)
          }}
        >
          {textos.botonCancelar}
        </Boton>
      </div>
    )
  }

  // «No, volver»: el foco regresa al botón que abrió el formulario
  const cerrar = () => {
    reset()
    cancelacion.reset()
    setAbierto(false)
    requestAnimationFrame(() => botonCancelar.current?.focus())
  }

  return (
    <form
      noValidate
      onSubmit={handleSubmit(({ motivo }) =>
        cancelacion.mutate(motivo, {
          onSuccess: () => {
            setAbierto(false)
            anunciar(vista === 'cliente' ? textos.pedidoCanceladoCliente : textos.pedidoCanceladoTienda)
          },
          onError: (fallo) => {
            if (!esErrorApi(fallo) || !fallo.esErrorDeValidacion) return
            const delMotivo = fallo.errores.find(({ campo }) => campo === 'motivo')
            if (delMotivo) setError('motivo', { message: delMotivo.mensaje }, { shouldFocus: true })
          },
        }),
      )}
      className="flex flex-col gap-3 rounded-control border-2 border-peligro p-4"
    >
      <div className="flex flex-col gap-1">
        <label htmlFor={idMotivo} className="font-medium">
          {textos.etiquetaMotivo}
        </label>
        <p id={`${idMotivo}-pista`} className="text-sm text-texto-suave">
          {vista === 'cliente' ? textos.pistaMotivoCliente : textos.pistaMotivoTienda}{' '}
          {textos.contadorMotivo(largo, LARGO_MAXIMO_MOTIVO)}
        </p>
        <textarea
          id={idMotivo}
          rows={3}
          maxLength={LARGO_MAXIMO_MOTIVO + 50}
          // El campo aparece porque la persona pidió cancelar: el foco va directo a escribir el motivo
          // eslint-disable-next-line jsx-a11y/no-autofocus
          autoFocus
          aria-invalid={errors.motivo ? true : undefined}
          aria-describedby={[`${idMotivo}-pista`, errors.motivo ? `${idMotivo}-error` : null].filter(Boolean).join(' ')}
          className={cn(
            'rounded-control border border-borde bg-fondo p-3 text-base',
            errors.motivo && 'border-peligro',
          )}
          {...register('motivo')}
        />
        {errors.motivo ? (
          <p id={`${idMotivo}-error`} className="text-sm font-medium text-peligro">
            {errors.motivo.message}
          </p>
        ) : null}
      </div>
      <p>{vista === 'cliente' ? textos.avisoCancelarCliente : textos.avisoCancelarTienda}</p>
      {alertaError}
      <div className="flex flex-wrap gap-2">
        <Boton type="submit" variante="peligro" enviando={cancelacion.isPending} textoEnviando={textos.cancelando}>
          {textos.botonConfirmarCancelacion}
        </Boton>
        <Boton type="button" variante="secundario" onClick={cerrar}>
          {textos.botonNoCancelar}
        </Boton>
      </div>
    </form>
  )
}
