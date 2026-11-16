import { zodResolver } from '@hookform/resolvers/zod'
import { Minus, Plus } from 'lucide-react'
import { useMemo } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { CampoTexto } from '@/components/CampoTexto'
import { enfocarCampo } from '@/components/enfocarCampo'
import { ResumenErrores } from '@/components/ResumenErrores'
import { Boton } from '@/components/ui/Boton'
import { cn } from '@/components/ui/utilidades'
import { esErrorApi } from '@/services/errores'
import { formatearPesos, formatearUnidades } from '@/services/formato'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { useResponderSolicitud, type RespuestaDeTienda, type SolicitudDisponible } from './api'
import {
  crearEsquemaResponder,
  TIPOS_RESPUESTA_CON_PRECIO,
  type DatosResponder,
  type EntradaResponder,
} from './esquemas'
import { textosSolicitudes as textos } from './textos'

type Campo = 'descripcionAlternativa' | 'precio' | 'cantidad'

const ids: Record<Campo, string> = {
  descripcionAlternativa: 'responder-alternativa',
  precio: 'responder-precio',
  cantidad: 'responder-cantidad',
}

/** Campos del backend (ResponderSolicitudRequest) → campos del formulario. */
const CAMPO_DEL_BACKEND: Record<string, Campo> = {
  precioEnPesos: 'precio',
  cantidadDisponible: 'cantidad',
  descripcionAlternativa: 'descripcionAlternativa',
}

/**
 * Responder «Sí lo tengo» o «Tengo algo parecido» (HU-019). Precio y unidades llegan precargados para que «Sí» quede en
 * 3 toques o menos (DECISIONES-FRONTEND §8): el último precio que dio la tienda (SC-011) y las unidades pedidas.
 * Errores según la sección 9: resumen arriba con enlace al campo y mensaje debajo de cada campo.
 */
export function FormularioResponder({
  solicitud,
  tipoInicial,
  alResponder,
}: {
  solicitud: SolicitudDisponible
  tipoInicial: DatosResponder['tipo']
  alResponder: (respuesta: RespuestaDeTienda) => void
}) {
  const respuesta = useResponderSolicitud()
  const esquema = useMemo(() => crearEsquemaResponder(solicitud.cantidad), [solicitud.cantidad])
  const campos = Object.keys(ids) as Campo[]

  const {
    register,
    control,
    handleSubmit,
    setError,
    setValue,
    formState: { errors, isSubmitted },
  } = useForm<EntradaResponder, unknown, DatosResponder>({
    resolver: zodResolver(esquema),
    mode: 'onTouched',
    shouldFocusError: false,
    defaultValues: {
      tipo: tipoInicial,
      precio: solicitud.precioSugeridoEnPesos === null ? '' : String(solicitud.precioSugeridoEnPesos),
      cantidad: String(solicitud.cantidad),
      descripcionAlternativa: '',
    },
  })

  const [tipo, precio, cantidad] = useWatch({ control, name: ['tipo', 'precio', 'cantidad'] })
  const vistaPrevia = esquema.safeParse({ tipo, precio, cantidad, descripcionAlternativa: 'vista previa' })
  const unidades = Number(cantidad)

  const enfocarPrimerError = (conError: string[]) => {
    const primero = campos.find((campo) => conError.includes(campo))
    if (primero) enfocarCampo(ids[primero])
  }

  const cambiarUnidades = (diferencia: number) => {
    const actual = Number.isInteger(unidades) ? unidades : solicitud.cantidad
    const nueva = Math.min(solicitud.cantidad, Math.max(1, actual + diferencia))
    setValue('cantidad', String(nueva), { shouldValidate: isSubmitted })
  }

  const enviar = (datos: DatosResponder) =>
    respuesta.mutate(
      { solicitudId: solicitud.id, ...datos },
      {
        onSuccess: alResponder,
        onError: (error) => {
          if (!esErrorApi(error)) return
          const marcados = error.errores.flatMap(({ campo, mensaje }) => {
            const propio = CAMPO_DEL_BACKEND[campo]
            return propio ? [{ campo: propio, mensaje }] : []
          })
          marcados.forEach(({ campo, mensaje }) => setError(campo, { message: mensaje }))
          enfocarPrimerError(marcados.map(({ campo }) => campo))
        },
      },
    )

  const error = respuesta.error
  const marcadoEnCampo = esErrorApi(error) && error.esErrorDeValidacion
  const errorGeneral =
    error && !marcadoEnCampo ? (esErrorApi(error) ? mensajeParaUsuario(error) : textos.errorResponder) : null
  const erroresResumen = isSubmitted
    ? campos.flatMap((campo) => {
        const mensaje = errors[campo]?.message
        return mensaje ? [{ idCampo: ids[campo], mensaje }] : []
      })
    : []
  const errorAlternativa = errors.descripcionAlternativa?.message

  return (
    <form
      noValidate
      onSubmit={handleSubmit(enviar, (fallas) => enfocarPrimerError(Object.keys(fallas)))}
      className="flex flex-col gap-5"
    >
      <ResumenErrores titulo={textos.tituloResumenErroresRespuesta} errores={erroresResumen} />

      {errorGeneral ? (
        <div role="alert" className="rounded-control border border-peligro p-3 font-medium text-peligro">
          <p>{errorGeneral}</p>
          {esErrorApi(error) && debeMostrarIdCorrelacion(error) ? (
            <p className="mt-1 text-sm font-normal text-texto-suave">{textos.codigoSoporte(error.idCorrelacion)}</p>
          ) : null}
        </div>
      ) : null}

      <fieldset className="flex flex-col gap-2">
        <legend className="mb-1 font-semibold">{textos.etiquetaTipoRespuesta}</legend>
        <div className="grid grid-cols-2 gap-2">
          {TIPOS_RESPUESTA_CON_PRECIO.map((opcion) => (
            <label
              key={opcion}
              className="flex min-h-tactil cursor-pointer items-center gap-2 rounded-control border border-borde p-3 has-checked:border-2 has-checked:border-primario has-checked:bg-superficie has-checked:font-semibold"
            >
              <input type="radio" value={opcion} className="size-5 accent-primario" {...register('tipo')} />
              {textos.tiposRespuesta[opcion]}
            </label>
          ))}
        </div>
      </fieldset>

      {tipo === 'PARECIDO' ? (
        <div className="flex flex-col gap-1">
          <label htmlFor={ids.descripcionAlternativa} className="font-medium">
            {textos.etiquetaAlternativa}
          </label>
          <p id={`${ids.descripcionAlternativa}-pista`} className="text-sm text-texto-suave">
            {textos.pistaAlternativa}
          </p>
          <textarea
            id={ids.descripcionAlternativa}
            rows={3}
            aria-invalid={errorAlternativa ? true : undefined}
            aria-describedby={[
              `${ids.descripcionAlternativa}-pista`,
              errorAlternativa ? `${ids.descripcionAlternativa}-error` : null,
            ]
              .filter(Boolean)
              .join(' ')}
            className={cn(
              'rounded-control border border-borde bg-fondo p-3 text-base',
              errorAlternativa && 'border-peligro',
            )}
            {...register('descripcionAlternativa')}
          />
          {errorAlternativa ? (
            <p id={`${ids.descripcionAlternativa}-error`} className="text-sm font-medium text-peligro">
              {errorAlternativa}
            </p>
          ) : null}
        </div>
      ) : null}

      <CampoTexto
        id={ids.precio}
        etiqueta={textos.etiquetaPrecio}
        pista={solicitud.precioSugeridoEnPesos === null ? textos.pistaPrecio : textos.pistaPrecioSugerido}
        error={errors.precio?.message}
        inputMode="numeric"
        autoComplete="off"
        {...register('precio')}
      />

      <div className="flex flex-col gap-1">
        <label htmlFor={ids.cantidad} className="font-medium">
          {textos.etiquetaCantidadDisponible}
        </label>
        <p id={`${ids.cantidad}-pista`} className="text-sm text-texto-suave">
          {textos.pistaCantidadDisponible(solicitud.cantidad)}
        </p>
        <div className="flex items-center gap-2">
          <Boton
            type="button"
            variante="secundario"
            aria-label={textos.quitarUnidad}
            aria-controls={ids.cantidad}
            disabled={unidades <= 1}
            onClick={() => cambiarUnidades(-1)}
          >
            <Minus aria-hidden="true" className="size-5" />
          </Boton>
          <input
            id={ids.cantidad}
            inputMode="numeric"
            autoComplete="off"
            aria-invalid={errors.cantidad ? true : undefined}
            aria-describedby={[`${ids.cantidad}-pista`, errors.cantidad ? `${ids.cantidad}-error` : null]
              .filter(Boolean)
              .join(' ')}
            className={cn(
              'min-h-tactil w-20 rounded-control border border-borde bg-fondo px-3 text-center text-base',
              errors.cantidad && 'border-peligro',
            )}
            {...register('cantidad')}
          />
          <Boton
            type="button"
            variante="secundario"
            aria-label={textos.agregarUnidad}
            aria-controls={ids.cantidad}
            disabled={unidades >= solicitud.cantidad}
            onClick={() => cambiarUnidades(1)}
          >
            <Plus aria-hidden="true" className="size-5" />
          </Boton>
        </div>
        {errors.cantidad ? (
          <p id={`${ids.cantidad}-error`} className="text-sm font-medium text-peligro">
            {errors.cantidad.message}
          </p>
        ) : null}
      </div>

      <div className="flex flex-col gap-2">
        {vistaPrevia.success ? (
          <p className="font-medium">
            {textos.clienteVera(formatearPesos(vistaPrevia.data.precio), formatearUnidades(vistaPrevia.data.cantidad))}
          </p>
        ) : null}
        <Boton
          type="submit"
          ancho="completo"
          enviando={respuesta.isPending}
          textoEnviando={textos.botonEnviandoRespuesta}
        >
          {textos.botonEnviarRespuesta}
        </Boton>
      </div>
    </form>
  )
}
