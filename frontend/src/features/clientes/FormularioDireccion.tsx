import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect, useId, useRef } from 'react'
import { Controller, useForm, type FieldPath } from 'react-hook-form'
import { CampoTexto } from '@/components/CampoTexto'
import { enfocarCampo } from '@/components/enfocarCampo'
import { ResumenErrores } from '@/components/ResumenErrores'
import { SelectorUbicacion } from '@/components/SelectorUbicacion'
import { Boton } from '@/components/ui/Boton'
import { esErrorApi } from '@/services/errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { useCrearDireccion, useEditarDireccion, type Direccion } from './api'
import { esquemaDireccion, type DatosDireccionFormulario, type EntradaDireccion } from './esquemas'
import { textosClientes as textos } from './textos'

type Campo = Exclude<FieldPath<EntradaDireccion>, `ubicacion.${string}`>

function valoresIniciales(direccion: Direccion | undefined, telefonoSugerido: string): EntradaDireccion {
  return direccion
    ? {
        nombre: direccion.nombre,
        ubicacion: { latitud: direccion.latitud, longitud: direccion.longitud },
        direccionEscrita: direccion.direccionEscrita,
        indicaciones: direccion.indicaciones ?? '',
        telefono: direccion.telefono,
      }
    : { nombre: '', ubicacion: null, direccionEscrita: '', indicaciones: '', telefono: telefonoSugerido }
}

/**
 * Guardar o editar una dirección (HU-007, CU-03 pasos 6 a 13). La ubicación se marca sin arrastrar: «Usar mi
 * ubicación» o un toque en el mapa (WCAG 2.5.7). El celular arranca con el del perfil para no escribirlo otra vez.
 */
export function FormularioDireccion({
  direccion,
  telefonoSugerido,
  alGuardar,
  alCancelar,
}: {
  direccion?: Direccion
  telefonoSugerido: string
  alGuardar: (nombre: string) => void
  alCancelar: () => void
}) {
  const creacion = useCrearDireccion()
  const edicion = useEditarDireccion()
  const guardado = direccion ? edicion : creacion
  const prefijo = useId()
  const ids: Record<Campo, string> = {
    nombre: `${prefijo}-nombre`,
    ubicacion: `${prefijo}-ubicacion`,
    direccionEscrita: `${prefijo}-direccion`,
    indicaciones: `${prefijo}-indicaciones`,
    telefono: `${prefijo}-telefono`,
  }
  const campos = Object.keys(ids) as Campo[]
  const titulo = useRef<HTMLHeadingElement>(null)

  // El formulario aparece porque la persona lo pidió: el foco va a su título para que sepa dónde está
  useEffect(() => titulo.current?.focus(), [])

  const {
    register,
    control,
    handleSubmit,
    setError,
    clearErrors,
    formState: { errors, isSubmitted },
  } = useForm<EntradaDireccion, unknown, DatosDireccionFormulario>({
    resolver: zodResolver(esquemaDireccion),
    mode: 'onTouched',
    shouldFocusError: false,
    defaultValues: valoresIniciales(direccion, telefonoSugerido),
  })

  const enfocarPrimerError = (conError: string[]) => {
    const primero = campos.find((campo) => conError.includes(campo))
    if (primero) enfocarCampo(ids[primero])
  }

  const enviar = (datos: DatosDireccionFormulario) => {
    const opciones = {
      onSuccess: () => alGuardar(datos.nombre),
      onError: (error: unknown) => {
        if (!esErrorApi(error)) return
        const marcados = error.errores.flatMap(({ campo, mensaje }) => {
          const principal = campo.split('.')[0] ?? ''
          return principal in ids ? [{ campo: principal as Campo, mensaje }] : []
        })
        marcados.forEach(({ campo, mensaje }) => setError(campo, { message: mensaje }))
        enfocarPrimerError(marcados.map(({ campo }) => campo))
      },
    }
    if (direccion) edicion.mutate({ direccionId: direccion.id, direccion: datos }, opciones)
    else creacion.mutate(datos, opciones)
  }

  const error = guardado.error
  const marcadoEnCampo = esErrorApi(error) && error.esErrorDeValidacion
  const errorGeneral =
    error && !marcadoEnCampo ? (esErrorApi(error) ? mensajeParaUsuario(error) : textos.errorGeneral) : null
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
      className="flex flex-col gap-4 rounded-control border border-borde p-4"
    >
      <h3 ref={titulo} tabIndex={-1} className="text-lg font-semibold">
        {direccion ? textos.tituloEditarDireccion(direccion.nombre) : textos.tituloNuevaDireccion}
      </h3>

      <ResumenErrores titulo={textos.tituloResumenErrores} errores={erroresResumen} />

      {errorGeneral ? (
        <div role="alert" className="rounded-control border border-peligro p-3 font-medium text-peligro">
          <p>{errorGeneral}</p>
          {esErrorApi(error) && debeMostrarIdCorrelacion(error) ? (
            <p className="mt-1 text-sm font-normal text-texto-suave">{textos.codigoSoporte(error.idCorrelacion)}</p>
          ) : null}
        </div>
      ) : null}

      <CampoTexto
        id={ids.nombre}
        etiqueta={textos.etiquetaNombreDireccion}
        pista={textos.pistaNombreDireccion}
        error={errors.nombre?.message}
        {...register('nombre')}
      />

      <Controller
        control={control}
        name="ubicacion"
        render={({ field }) => (
          <SelectorUbicacion
            id={ids.ubicacion}
            etiqueta={textos.etiquetaUbicacion}
            pista={textos.pistaUbicacion}
            valor={field.value}
            alCambiar={(coordenadas) => {
              field.onChange(coordenadas)
              clearErrors('ubicacion')
            }}
            error={errors.ubicacion?.message}
          />
        )}
      />

      <CampoTexto
        id={ids.direccionEscrita}
        etiqueta={textos.etiquetaDireccionEscrita}
        pista={textos.pistaDireccionEscrita}
        autoComplete="street-address"
        error={errors.direccionEscrita?.message}
        {...register('direccionEscrita')}
      />
      <CampoTexto
        id={ids.indicaciones}
        etiqueta={textos.etiquetaIndicaciones}
        pista={textos.pistaIndicaciones}
        error={errors.indicaciones?.message}
        {...register('indicaciones')}
      />
      <CampoTexto
        id={ids.telefono}
        etiqueta={textos.etiquetaTelefonoDireccion}
        type="tel"
        inputMode="tel"
        autoComplete="tel"
        error={errors.telefono?.message}
        {...register('telefono')}
      />

      <div className="flex flex-wrap gap-2">
        <Boton type="submit" enviando={guardado.isPending} textoEnviando={textos.botonGuardando}>
          {textos.botonGuardarDireccion}
        </Boton>
        <Boton type="button" variante="secundario" onClick={alCancelar}>
          {textos.botonCancelar}
        </Boton>
      </div>
    </form>
  )
}
