import { zodResolver } from '@hookform/resolvers/zod'
import { Star } from 'lucide-react'
import { useForm, useWatch } from 'react-hook-form'
import { enfocarCampo } from '@/components/enfocarCampo'
import { ResumenErrores } from '@/components/ResumenErrores'
import { Boton } from '@/components/ui/Boton'
import { cn } from '@/components/ui/utilidades'
import { esErrorApi } from '@/services/errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { useCalificar, type Resena } from './api'
import {
  esquemaCalificar,
  LARGO_MAXIMO_COMENTARIO,
  OPCIONES_ESTRELLAS,
  type DatosCalificar,
  type EntradaCalificar,
} from './esquemas'
import { textosReputacion as textos } from './textos'

type Campo = keyof EntradaCalificar
const ID_CAMPO: Record<Campo, string> = { estrellas: 'calificar-estrellas', comentario: 'calificar-comentario' }
const CAMPOS = Object.keys(ID_CAMPO) as Campo[]

/**
 * HU-034: de 1 a 5 estrellas y un comentario opcional (mockup 03). Las estrellas son un grupo de radios: se eligen
 * con un toque o con las flechas del teclado, cada una mide 44 × 44 px o más y su nombre es «4 estrellas». Arriba
 * se dice en texto cuántas van elegidas. Errores según §9: en el campo y en el resumen, con el foco en el primero.
 */
export function FormularioCalificar({
  ordenId,
  alPublicar,
}: {
  ordenId: string
  alPublicar: (resena: Resena) => void
}) {
  const calificacion = useCalificar(ordenId)
  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitted },
  } = useForm<EntradaCalificar, unknown, DatosCalificar>({
    resolver: zodResolver(esquemaCalificar),
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
    defaultValues: { estrellas: undefined, comentario: '' },
  })
  const elegidas = Number(useWatch({ control, name: 'estrellas' }) ?? 0)
  const largo = (useWatch({ control, name: 'comentario' }) ?? '').length

  const enfocarPrimerError = (campos: Campo[]) => {
    const primero = CAMPOS.find((campo) => campos.includes(campo))
    if (primero) enfocarCampo(ID_CAMPO[primero])
  }

  const enviar = (datos: DatosCalificar) =>
    calificacion.mutate(
      { estrellas: datos.estrellas, ...(datos.comentario ? { comentario: datos.comentario } : {}) },
      {
        onSuccess: alPublicar,
        onError: (error) => {
          if (!esErrorApi(error) || !error.esErrorDeValidacion) return
          const marcados = error.errores.filter(({ campo }) => campo === 'estrellas' || campo === 'comentario')
          marcados.forEach(({ campo, mensaje }) => setError(campo as Campo, { message: mensaje }))
          enfocarPrimerError(marcados.map(({ campo }) => campo as Campo))
        },
      },
    )

  const error = calificacion.error
  const errorGeneral = error && !(esErrorApi(error) && error.esErrorDeValidacion) ? error : null
  const erroresResumen = isSubmitted
    ? CAMPOS.flatMap((campo) => {
        const mensaje = errors[campo]?.message
        return mensaje ? [{ idCampo: ID_CAMPO[campo], mensaje }] : []
      })
    : []

  return (
    <form
      noValidate
      onSubmit={handleSubmit(enviar, (fallas) => enfocarPrimerError(Object.keys(fallas) as Campo[]))}
      className="flex flex-col gap-5"
    >
      <ResumenErrores titulo={textos.tituloResumenErrores} errores={erroresResumen} />

      <fieldset
        id={ID_CAMPO.estrellas}
        aria-describedby={errors.estrellas ? `${ID_CAMPO.estrellas}-error` : undefined}
        className="flex flex-col gap-2"
      >
        <legend className="sr-only">{textos.etiquetaCalificacion}</legend>
        <p aria-hidden="true" className="font-semibold">
          {elegidas > 0 ? textos.calificacionElegida(elegidas) : textos.calificacionSinElegir}
        </p>
        <div className="flex flex-wrap gap-2">
          {OPCIONES_ESTRELLAS.map((opcion) => {
            const numero = Number(opcion)
            const llena = numero <= elegidas
            return (
              <label
                key={opcion}
                className={cn(
                  'flex size-14 cursor-pointer items-center justify-center rounded-control border has-focus-visible:outline-2 has-focus-visible:outline-offset-2 has-focus-visible:outline-primario',
                  llena ? 'border-acento bg-superficie' : 'border-borde',
                  errors.estrellas && 'border-peligro',
                )}
              >
                <input
                  type="radio"
                  value={opcion}
                  aria-label={textos.estrellas(numero)}
                  className="sr-only"
                  {...register('estrellas')}
                />
                <Star
                  aria-hidden="true"
                  className={cn('size-7', llena ? 'fill-acento text-acento' : 'text-texto-suave')}
                />
              </label>
            )
          })}
        </div>
        {errors.estrellas ? (
          <p id={`${ID_CAMPO.estrellas}-error`} className="text-sm font-medium text-peligro">
            {errors.estrellas.message}
          </p>
        ) : null}
      </fieldset>

      <div className="flex flex-col gap-1">
        <label htmlFor={ID_CAMPO.comentario} className="font-semibold">
          {textos.etiquetaComentario}
        </label>
        <p id={`${ID_CAMPO.comentario}-pista`} className="text-sm text-texto-suave">
          {textos.pistaComentario} {textos.contadorComentario(largo, LARGO_MAXIMO_COMENTARIO)}
        </p>
        <textarea
          id={ID_CAMPO.comentario}
          rows={4}
          maxLength={LARGO_MAXIMO_COMENTARIO + 50}
          aria-invalid={errors.comentario ? true : undefined}
          aria-describedby={[`${ID_CAMPO.comentario}-pista`, errors.comentario ? `${ID_CAMPO.comentario}-error` : null]
            .filter(Boolean)
            .join(' ')}
          className={cn(
            'rounded-control border border-borde bg-fondo p-3 text-base',
            errors.comentario && 'border-peligro',
          )}
          {...register('comentario')}
        />
        {errors.comentario ? (
          <p id={`${ID_CAMPO.comentario}-error`} className="text-sm font-medium text-peligro">
            {errors.comentario.message}
          </p>
        ) : null}
      </div>

      {errorGeneral ? (
        <div role="alert" className="rounded-control border border-peligro p-3 font-medium text-peligro">
          <p>{esErrorApi(errorGeneral) ? mensajeParaUsuario(errorGeneral) : textos.errorCalificar}</p>
          {esErrorApi(errorGeneral) && debeMostrarIdCorrelacion(errorGeneral) ? (
            <p className="mt-1 text-sm font-normal text-texto-suave">
              {textos.codigoSoporte(errorGeneral.idCorrelacion)}
            </p>
          ) : null}
        </div>
      ) : null}

      <Boton type="submit" ancho="completo" enviando={calificacion.isPending} textoEnviando={textos.publicando}>
        {textos.botonPublicar}
      </Boton>
    </form>
  )
}
