import { zodResolver } from '@hookform/resolvers/zod'
import { ShieldOff, ShieldCheck } from 'lucide-react'
import { useId, useRef, useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { cn } from '@/components/ui/utilidades'
import { numeroCorto } from '@/features/ordenes/presentacion'
import { formatearFechaHora } from '@/services/formato'
import {
  mensajeReportes,
  useReactivarTienda,
  useResolverReporte,
  useSuspenderTienda,
  type EstadoTienda,
  type ReporteParaAdministrador,
} from './api'
import {
  esquemaResolverReporte,
  esquemaSuspender,
  LARGO_MAXIMO_MOTIVO_SUSPENSION,
  type DatosResolverReporte,
  type DatosSuspender,
} from './esquemas'
import { textosAdministracion as textos } from './textos'

/** La tienda del reporte: nombre y estado vienen en el reporte (nombreTienda, estadoTienda). */
export interface TiendaDelReporte {
  nombre: string
  estado: EstadoTienda | null
}

/** CP-ADM-037-02: nota de resolución (3 a 500) y «Marcar como resuelto» (mockup 07). */
function ResolverReporte({ reporte }: { reporte: ReporteParaAdministrador }) {
  const anunciar = useAnunciar()
  const resolver = useResolverReporte()
  const idNota = useId()
  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<DatosResolverReporte>({
    resolver: zodResolver(esquemaResolverReporte),
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    defaultValues: { nota: '' },
  })
  const largo = useWatch({ control, name: 'nota' }).length

  return (
    <form
      noValidate
      onSubmit={handleSubmit(({ nota }) =>
        resolver.mutate({ reporteId: reporte.id, nota }, { onSuccess: () => anunciar(textos.reporteResuelto) }),
      )}
      className="flex flex-col gap-2"
    >
      <label htmlFor={idNota} className="font-semibold">
        {textos.etiquetaNota}
      </label>
      <p id={`${idNota}-pista`} className="text-sm text-texto-suave">
        {textos.pistaNota} {textos.contadorNota(largo)}
      </p>
      <textarea
        id={idNota}
        rows={3}
        maxLength={550}
        aria-invalid={errors.nota ? true : undefined}
        aria-describedby={[`${idNota}-pista`, errors.nota ? `${idNota}-error` : null].filter(Boolean).join(' ')}
        className={cn('rounded-control border border-borde bg-fondo p-3 text-base', errors.nota && 'border-peligro')}
        {...register('nota')}
      />
      {errors.nota ? (
        <p id={`${idNota}-error`} className="text-sm font-medium text-peligro">
          {errors.nota.message}
        </p>
      ) : null}
      {resolver.error ? (
        <p role="alert" className="font-medium text-peligro">
          {mensajeReportes(resolver.error)}
        </p>
      ) : null}
      <Boton type="submit" ancho="completo" enviando={resolver.isPending} textoEnviando={textos.resolviendo}>
        {textos.botonResolver}
      </Boton>
    </form>
  )
}

/**
 * CP-ADM-037-01 y SC-011 punto 8: suspender pide confirmación con motivo en el mismo lugar, con lo que va a pasar
 * dicho antes del botón; reactivar también confirma. «No, volver» cierra y devuelve el foco al botón.
 */
function AccionSobreLaTienda({ tiendaId, tienda }: { tiendaId: string; tienda: TiendaDelReporte }) {
  const anunciar = useAnunciar()
  const suspender = useSuspenderTienda()
  const reactivar = useReactivarTienda()
  const [confirmando, setConfirmando] = useState(false)
  const boton = useRef<HTMLButtonElement>(null)
  const idMotivo = useId()
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<DatosSuspender>({
    resolver: zodResolver(esquemaSuspender),
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    defaultValues: { motivo: '' },
  })
  const suspendida = tienda.estado === 'SUSPENDIDA'
  const error = suspendida ? reactivar.error : suspender.error

  if (tienda.estado !== 'APROBADA' && tienda.estado !== 'SUSPENDIDA') return null

  const volver = () => {
    reset()
    suspender.reset()
    reactivar.reset()
    setConfirmando(false)
    requestAnimationFrame(() => boton.current?.focus())
  }

  if (!confirmando) {
    return (
      <div className="flex flex-col gap-1">
        {error ? (
          <p role="alert" className="font-medium text-peligro">
            {mensajeReportes(error)}
          </p>
        ) : null}
        <Boton
          ref={boton}
          type="button"
          variante={suspendida ? 'secundario' : 'peligro'}
          ancho="completo"
          onClick={() => setConfirmando(true)}
        >
          {suspendida ? (
            <ShieldCheck aria-hidden="true" className="size-5" />
          ) : (
            <ShieldOff aria-hidden="true" className="size-5" />
          )}
          {suspendida ? textos.botonReactivar(tienda.nombre) : textos.botonSuspender(tienda.nombre)}
        </Boton>
        {suspendida ? null : <p className="text-center text-sm text-texto-suave">{textos.pistaSuspender}</p>}
      </div>
    )
  }

  if (suspendida) {
    return (
      <div className="flex flex-col gap-3 rounded-control border-2 border-primario p-3">
        <p>{textos.avisoReactivar(tienda.nombre)}</p>
        {reactivar.error ? (
          <p role="alert" className="font-medium text-peligro">
            {mensajeReportes(reactivar.error)}
          </p>
        ) : null}
        <div className="flex flex-wrap gap-2">
          <Boton
            type="button"
            enviando={reactivar.isPending}
            textoEnviando={textos.reactivando}
            onClick={() =>
              reactivar.mutate(tiendaId, {
                onSuccess: () => {
                  setConfirmando(false)
                  anunciar(textos.tiendaReactivada(tienda.nombre))
                },
              })
            }
          >
            {textos.botonConfirmarReactivacion(tienda.nombre)}
          </Boton>
          <Boton type="button" variante="secundario" onClick={volver}>
            {textos.botonNoVolver}
          </Boton>
        </div>
      </div>
    )
  }

  return (
    <form
      noValidate
      onSubmit={handleSubmit(({ motivo }) =>
        suspender.mutate(
          { tiendaId, motivo },
          {
            onSuccess: () => {
              setConfirmando(false)
              reset()
              anunciar(textos.tiendaSuspendidaAnuncio(tienda.nombre))
            },
          },
        ),
      )}
      className="flex flex-col gap-3 rounded-control border-2 border-peligro p-3"
    >
      <div className="flex flex-col gap-1">
        <label htmlFor={idMotivo} className="font-semibold">
          {textos.etiquetaMotivoSuspension}
        </label>
        <p id={`${idMotivo}-pista`} className="text-sm text-texto-suave">
          {textos.pistaMotivoSuspension}
        </p>
        <textarea
          id={idMotivo}
          rows={3}
          maxLength={LARGO_MAXIMO_MOTIVO_SUSPENSION + 50}
          // El campo aparece porque la persona pidió suspender: el foco va directo a escribir el motivo
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
      <p className="font-medium">{textos.avisoSuspender(tienda.nombre)}</p>
      {suspender.error ? (
        <p role="alert" className="font-medium text-peligro">
          {mensajeReportes(suspender.error)}
        </p>
      ) : null}
      <div className="flex flex-wrap gap-2">
        <Boton type="submit" variante="peligro" enviando={suspender.isPending} textoEnviando={textos.suspendiendo}>
          {textos.botonConfirmarSuspension(tienda.nombre)}
        </Boton>
        <Boton type="button" variante="secundario" onClick={volver}>
          {textos.botonNoVolver}
        </Boton>
      </div>
    </form>
  )
}

/**
 * Un reporte para el administrador (HU-037, mockup 07): qué pasó, quién reportó y cuándo, la tienda con cuántos
 * reportes tiene, la descripción y, si está abierto, la nota para resolverlo. Al final, suspender o reactivar la
 * tienda. El estado del reporte va en texto («Abierto», «Resuelto»), no solo con color.
 */
export function TarjetaReporte({ reporte, tienda }: { reporte: ReporteParaAdministrador; tienda: TiendaDelReporte }) {
  const idTitulo = useId()
  const abierto = reporte.estado === 'ABIERTO'
  return (
    <article aria-labelledby={idTitulo} className="flex flex-col gap-4 rounded-control border border-borde p-4">
      <div className="flex flex-col gap-1">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h2 id={idTitulo} className="text-xl font-semibold">
            {textos.motivosReporte[reporte.motivo]}
          </h2>
          <p
            className={cn(
              'rounded-full border px-3 py-0.5 text-sm font-semibold',
              abierto ? 'border-advertencia text-advertencia' : 'border-exito text-exito',
            )}
          >
            {textos.estadoReporteSingular[reporte.estado]}
          </p>
        </div>
        <p className="text-texto-suave">
          {textos.reportadoPor[reporte.reportadoPor]} · {formatearFechaHora(reporte.fecha)} ·{' '}
          {textos.pedido(numeroCorto(reporte.ordenId))}
        </p>
      </div>

      <div className="flex flex-col gap-1 rounded-control border border-borde p-3">
        <p className="font-semibold">{tienda.nombre}</p>
        <p>{textos.reportesDeLaTienda(reporte.reportesDeLaTienda)}</p>
        {tienda.estado === 'SUSPENDIDA' ? (
          <p className="font-semibold text-peligro">{textos.tiendaSuspendida}</p>
        ) : null}
        <p className={cn('break-words', !reporte.descripcion && 'text-texto-suave')}>
          {reporte.descripcion ? `«${reporte.descripcion}»` : textos.sinDescripcion}
        </p>
      </div>

      {abierto ? (
        <ResolverReporte reporte={reporte} />
      ) : (
        <div className="flex flex-col gap-1">
          {reporte.fechaResolucion ? (
            <p className="text-texto-suave">{textos.resueltoEl(formatearFechaHora(reporte.fechaResolucion))}</p>
          ) : null}
          {reporte.notaResolucion ? <p className="break-words">{textos.nota(reporte.notaResolucion)}</p> : null}
        </div>
      )}

      <AccionSobreLaTienda tiendaId={reporte.tiendaId} tienda={tienda} />
    </article>
  )
}
