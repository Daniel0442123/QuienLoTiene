import { zodResolver } from '@hookform/resolvers/zod'
import type { ReactNode } from 'react'
import { Link } from 'react-router'
import { useForm, useWatch, type FieldError } from 'react-hook-form'
import { enfocarCampo } from '@/components/enfocarCampo'
import { ResumenErrores } from '@/components/ResumenErrores'
import { Boton } from '@/components/ui/Boton'
import { cn } from '@/components/ui/utilidades'
import type { RespuestaParaCliente } from '@/features/solicitudes/api'
import { useDirecciones } from '@/hooks/useDirecciones'
import { esErrorApi } from '@/services/errores'
import { formatearPesos, formatearUnidades } from '@/services/formato'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { useCrearOrden, type Orden } from './api'
import { esquemaConfirmarPedido, MEDIOS_PAGO, type DatosConfirmarPedido, type EntradaConfirmarPedido } from './esquemas'
import { textosOrdenes as textos } from './textos'

type Campo = 'direccionId' | 'medioPago'

const ids: Record<Campo, string> = { direccionId: 'pedido-direccion', medioPago: 'pedido-medio-pago' }

/** Errores del backend que corresponden a un campo del formulario, aunque lleguen sin `errores` (ORD-003, TIE-010). */
const CAMPO_POR_CODIGO: Record<string, Campo> = {
  'ORD-003': 'medioPago',
  'ORD-004': 'direccionId',
  'TIE-010': 'direccionId',
  'CLI-003': 'direccionId',
}

const claseOpcion =
  'flex min-h-tactil cursor-pointer items-center gap-3 rounded-control border p-3 has-checked:border-2 has-checked:border-primario has-checked:bg-superficie'

function GrupoOpciones({
  id,
  leyenda,
  pista,
  error,
  children,
}: {
  id: string
  leyenda: string
  pista?: string
  error?: FieldError
  children: ReactNode
}) {
  const descritoPor = [pista ? `${id}-pista` : null, error ? `${id}-error` : null].filter(Boolean).join(' ')
  return (
    <fieldset id={id} className="flex flex-col gap-2" aria-describedby={descritoPor || undefined}>
      <legend className="mb-1 font-semibold">{leyenda}</legend>
      {pista ? (
        <p id={`${id}-pista`} className="text-sm text-texto-suave">
          {pista}
        </p>
      ) : null}
      {children}
      {error ? (
        <p id={`${id}-error`} className="text-sm font-medium text-peligro">
          {error.message}
        </p>
      ) : null}
    </fieldset>
  )
}

/**
 * Confirmar el pedido (HU-024, diagrama 04): cómo se recibe, a qué dirección y cómo se paga, con el total desglosado
 * (CP-ORD-024-01). Avisa antes de confirmar que la tienda verá los datos del cliente (Ley 1581, CP-ORD-024-02).
 */
export function FormularioConfirmarPedido({
  respuesta,
  modalidadSolicitada,
  alConfirmar,
}: {
  respuesta: RespuestaParaCliente
  modalidadSolicitada: 'RECOGER' | 'DOMICILIO'
  alConfirmar: (orden: Orden) => void
}) {
  const creacion = useCrearOrden()
  const direcciones = useDirecciones()
  const conDomicilio = respuesta.ofreceDomicilio && respuesta.costoDomicilioEnPesos !== null
  const campos = Object.keys(ids) as Campo[]

  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitted },
  } = useForm<EntradaConfirmarPedido, unknown, DatosConfirmarPedido>({
    resolver: zodResolver(esquemaConfirmarPedido),
    mode: 'onTouched',
    shouldFocusError: false,
    defaultValues: {
      modalidad: modalidadSolicitada === 'DOMICILIO' && conDomicilio ? 'DOMICILIO' : 'RECOGER',
      direccionId: null,
      medioPago: null,
    },
    // La primera dirección guardada ya viene elegida (WCAG 3.3.7: no pedir otra vez lo que ya se dio)
    values: direcciones.data?.[0]
      ? {
          modalidad: modalidadSolicitada === 'DOMICILIO' && conDomicilio ? 'DOMICILIO' : 'RECOGER',
          direccionId: direcciones.data[0].id,
          medioPago: null,
        }
      : undefined,
    resetOptions: { keepDirtyValues: true },
  })
  const modalidad = useWatch({ control, name: 'modalidad' })

  const domicilio = modalidad === 'DOMICILIO' ? (respuesta.costoDomicilioEnPesos ?? 0) : 0
  const total = respuesta.subtotalEnPesos + domicilio
  // DEF-007 (CP-TIE-012-01): solo los medios que la tienda acepta. Si la lista viene vacía (la tienda no los
  // configuró), los tres; el backend igual rechaza uno que no aplique (ORD-003) y el error sale en este grupo.
  const aceptados = respuesta.mediosPagoAceptados
  const mediosPago = aceptados.length > 0 ? MEDIOS_PAGO.filter((medio) => aceptados.includes(medio)) : MEDIOS_PAGO

  const enfocarPrimerError = (conError: string[]) => {
    const primero = campos.find((campo) => conError.includes(campo))
    if (primero) enfocarCampo(ids[primero])
  }

  const enviar = (datos: DatosConfirmarPedido) =>
    creacion.mutate(
      { ...datos, respuestaId: respuesta.id },
      {
        onSuccess: alConfirmar,
        onError: (error) => {
          if (!esErrorApi(error)) return
          const campo = CAMPO_POR_CODIGO[error.codigo]
          if (!campo) return
          setError(campo, { message: mensajeParaUsuario(error) })
          enfocarCampo(ids[campo])
        },
      },
    )

  const error = creacion.error
  const enCampo = esErrorApi(error) && Boolean(CAMPO_POR_CODIGO[error.codigo])
  const errorGeneral = error && !enCampo ? (esErrorApi(error) ? mensajeParaUsuario(error) : textos.errorGeneral) : null
  const erroresResumen = isSubmitted
    ? campos.flatMap((campo) => {
        const mensaje = errors[campo]?.message
        return mensaje ? [{ idCampo: ids[campo], mensaje }] : []
      })
    : []

  return (
    <form
      noValidate
      onSubmit={handleSubmit(enviar, (fallas) => enfocarPrimerError(Object.keys(fallas)))}
      className="flex flex-col gap-5"
    >
      <ResumenErrores titulo={textos.tituloResumenErrores} errores={erroresResumen} />

      {errorGeneral ? (
        <div role="alert" className="rounded-control border border-peligro p-3 font-medium text-peligro">
          <p>{errorGeneral}</p>
          {esErrorApi(error) && debeMostrarIdCorrelacion(error) ? (
            <p className="mt-1 text-sm font-normal text-texto-suave">{textos.codigoSoporte(error.idCorrelacion)}</p>
          ) : null}
        </div>
      ) : null}

      <GrupoOpciones id="pedido-modalidad" leyenda={textos.etiquetaModalidad}>
        {conDomicilio ? (
          <label className={cn(claseOpcion, 'border-borde')}>
            <input type="radio" value="DOMICILIO" className="size-5 accent-primario" {...register('modalidad')} />
            {textos.modalidadDomicilio(formatearPesos(respuesta.costoDomicilioEnPesos ?? 0))}
          </label>
        ) : (
          <p className="text-texto-suave">{textos.sinDomicilio}</p>
        )}
        <label className={cn(claseOpcion, 'border-borde')}>
          <input type="radio" value="RECOGER" className="size-5 accent-primario" {...register('modalidad')} />
          {textos.modalidadRecoger}
        </label>
      </GrupoOpciones>

      {modalidad === 'DOMICILIO' ? (
        <GrupoOpciones id={ids.direccionId} leyenda={textos.etiquetaDireccion} error={errors.direccionId}>
          {direcciones.isPending ? (
            <p role="status">{textos.cargandoDirecciones}</p>
          ) : (direcciones.data ?? []).length === 0 ? (
            <p>
              {textos.sinDirecciones}{' '}
              <Link to="/cuenta" className="text-primario underline underline-offset-4 hover:no-underline">
                {textos.enlaceAgregarDireccion}
              </Link>
            </p>
          ) : (
            (direcciones.data ?? []).map((direccion) => (
              <label
                key={direccion.id}
                className={cn(claseOpcion, errors.direccionId ? 'border-peligro' : 'border-borde')}
              >
                <input
                  type="radio"
                  value={direccion.id}
                  className="size-5 accent-primario"
                  {...register('direccionId')}
                />
                <span className="flex flex-col">
                  <span className="font-medium">{direccion.nombre}</span>
                  <span className="text-sm break-words text-texto-suave">{direccion.direccionEscrita}</span>
                </span>
              </label>
            ))
          )}
        </GrupoOpciones>
      ) : null}

      <GrupoOpciones
        id={ids.medioPago}
        leyenda={textos.etiquetaMedioPago}
        pista={textos.pistaMedioPago}
        error={errors.medioPago}
      >
        {mediosPago.map((medio) => (
          <label key={medio} className={cn(claseOpcion, errors.medioPago ? 'border-peligro' : 'border-borde')}>
            <input type="radio" value={medio} className="size-5 accent-primario" {...register('medioPago')} />
            {textos.mediosPago[medio]}
          </label>
        ))}
      </GrupoOpciones>

      <dl className="flex flex-col gap-1 rounded-control bg-superficie p-4">
        <div className="flex justify-between gap-3">
          <dt>
            {textos.producto(formatearPesos(respuesta.precioEnPesos), formatearUnidades(respuesta.cantidadDisponible))}
          </dt>
          <dd>{formatearPesos(respuesta.subtotalEnPesos)}</dd>
        </div>
        <div className="flex justify-between gap-3">
          <dt>{textos.domicilio}</dt>
          <dd>{modalidad === 'DOMICILIO' ? formatearPesos(domicilio) : textos.domicilioNoAplica}</dd>
        </div>
        <div className="flex justify-between gap-3 border-t border-borde pt-2 font-bold">
          <dt>{textos.total}</dt>
          <dd>{formatearPesos(total)}</dd>
        </div>
      </dl>

      <p className="text-texto-suave">{textos.avisoDatos}</p>

      <Boton type="submit" ancho="completo" enviando={creacion.isPending} textoEnviando={textos.botonConfirmando}>
        {textos.botonConfirmar(formatearPesos(total))}
      </Boton>
    </form>
  )
}
