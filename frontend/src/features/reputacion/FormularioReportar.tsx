import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, useWatch } from 'react-hook-form'
import { enfocarCampo } from '@/components/enfocarCampo'
import { ResumenErrores } from '@/components/ResumenErrores'
import { Boton } from '@/components/ui/Boton'
import { cn } from '@/components/ui/utilidades'
import { esErrorApi } from '@/services/errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { useReportar, type Reporte } from './api'
import {
  esquemaReportar,
  LARGO_MAXIMO_DESCRIPCION,
  MOTIVOS_REPORTE,
  type DatosReportar,
  type EntradaReportar,
} from './esquemas'
import { textosReputacion as textos } from './textos'

type Campo = keyof EntradaReportar
const ID_CAMPO: Record<Campo, string> = { motivo: 'reporte-motivo', descripcion: 'reporte-descripcion' }
const CAMPOS = Object.keys(ID_CAMPO) as Campo[]

const claseOpcion =
  'flex min-h-tactil cursor-pointer items-center gap-3 rounded-control border px-3 has-checked:border-primario has-checked:bg-superficie has-checked:font-semibold'

/**
 * HU-035 (mockup 03): qué pasó, de la lista de SC-011 punto 5, y la descripción. La descripción es opcional salvo con
 * «Otro problema», y la etiqueta lo dice al elegirlo. Errores según §9: en el campo y en el resumen, con el foco en
 * el primero.
 */
export function FormularioReportar({ ordenId, alEnviar }: { ordenId: string; alEnviar: (reporte: Reporte) => void }) {
  const reporte = useReportar(ordenId)
  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitted },
  } = useForm<EntradaReportar, unknown, DatosReportar>({
    resolver: zodResolver(esquemaReportar),
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
    defaultValues: { motivo: undefined, descripcion: '' },
  })
  const motivo = useWatch({ control, name: 'motivo' })
  const largo = (useWatch({ control, name: 'descripcion' }) ?? '').length
  const obligatoria = motivo === 'OTRO'

  const enfocarPrimerError = (campos: string[]) => {
    const primero = CAMPOS.find((campo) => campos.includes(campo))
    if (primero) enfocarCampo(ID_CAMPO[primero])
  }

  const enviar = (datos: DatosReportar) =>
    reporte.mutate(datos, {
      onSuccess: alEnviar,
      onError: (error) => {
        if (!esErrorApi(error) || !error.esErrorDeValidacion) return
        const marcados = error.errores.filter(({ campo }) => campo === 'motivo' || campo === 'descripcion')
        marcados.forEach(({ campo, mensaje }) => setError(campo as Campo, { message: mensaje }))
        enfocarPrimerError(marcados.map(({ campo }) => campo))
      },
    })

  const error = reporte.error
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
      onSubmit={handleSubmit(enviar, (fallas) => enfocarPrimerError(Object.keys(fallas)))}
      className="flex flex-col gap-5"
    >
      <ResumenErrores titulo={textos.tituloResumenReporte} errores={erroresResumen} />

      <fieldset
        id={ID_CAMPO.motivo}
        aria-describedby={errors.motivo ? `${ID_CAMPO.motivo}-error` : undefined}
        className="flex flex-col gap-2"
      >
        <legend className="mb-1 font-semibold">{textos.etiquetaMotivo}</legend>
        {MOTIVOS_REPORTE.map((opcion) => (
          <label key={opcion} className={cn(claseOpcion, errors.motivo ? 'border-peligro' : 'border-borde')}>
            <input type="radio" value={opcion} className="size-5 accent-primario" {...register('motivo')} />
            {textos.motivos[opcion]}
          </label>
        ))}
        {errors.motivo ? (
          <p id={`${ID_CAMPO.motivo}-error`} className="text-sm font-medium text-peligro">
            {errors.motivo.message}
          </p>
        ) : null}
      </fieldset>

      <div className="flex flex-col gap-1">
        <label htmlFor={ID_CAMPO.descripcion} className="font-semibold">
          {obligatoria ? textos.etiquetaDescripcionObligatoria : textos.etiquetaDescripcionOpcional}
        </label>
        <p id={`${ID_CAMPO.descripcion}-pista`} className="text-sm text-texto-suave">
          {textos.pistaDescripcion} {textos.contadorDescripcion(largo, LARGO_MAXIMO_DESCRIPCION)}
        </p>
        <textarea
          id={ID_CAMPO.descripcion}
          rows={4}
          maxLength={LARGO_MAXIMO_DESCRIPCION + 50}
          aria-required={obligatoria || undefined}
          aria-invalid={errors.descripcion ? true : undefined}
          aria-describedby={[
            `${ID_CAMPO.descripcion}-pista`,
            errors.descripcion ? `${ID_CAMPO.descripcion}-error` : null,
          ]
            .filter(Boolean)
            .join(' ')}
          className={cn(
            'rounded-control border border-borde bg-fondo p-3 text-base',
            errors.descripcion && 'border-peligro',
          )}
          {...register('descripcion')}
        />
        {errors.descripcion ? (
          <p id={`${ID_CAMPO.descripcion}-error`} className="text-sm font-medium text-peligro">
            {errors.descripcion.message}
          </p>
        ) : null}
      </div>

      {errorGeneral ? (
        <div role="alert" className="rounded-control border border-peligro p-3 font-medium text-peligro">
          <p>{esErrorApi(errorGeneral) ? mensajeParaUsuario(errorGeneral) : textos.errorReportar}</p>
          {esErrorApi(errorGeneral) && debeMostrarIdCorrelacion(errorGeneral) ? (
            <p className="mt-1 text-sm font-normal text-texto-suave">
              {textos.codigoSoporte(errorGeneral.idCorrelacion)}
            </p>
          ) : null}
        </div>
      ) : null}

      <Boton type="submit" ancho="completo" enviando={reporte.isPending} textoEnviando={textos.enviandoReporte}>
        {textos.botonEnviarReporte}
      </Boton>
    </form>
  )
}
