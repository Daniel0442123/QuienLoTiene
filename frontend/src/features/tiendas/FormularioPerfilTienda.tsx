import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useFieldArray, useForm, useWatch } from 'react-hook-form'
import { CampoTexto } from '@/components/CampoTexto'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { cn } from '@/components/ui/utilidades'
import { esErrorApi } from '@/services/errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { useActualizarPerfilTienda, type MiTienda } from './api'
import { esquemaPerfilTienda, perfilInicial, type DatosPerfilTienda, type EntradaPerfilTienda } from './esquemas'
import { textosTiendas as textos } from './textos'

function conMayuscula(texto: string): string {
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

/**
 * Perfil y horario de la tienda (HU-009, CU-16 pasos 6 a 13). Un día por fila: se marca si abre y a qué horas.
 * Los errores del backend (GEN-001 de nombre o teléfono, TIE-007 del horario) quedan en su campo o arriba del horario.
 */
export function FormularioPerfilTienda({ tienda }: { tienda: MiTienda }) {
  const anunciar = useAnunciar()
  const guardado = useActualizarPerfilTienda()
  const [huboCambios, setHuboCambios] = useState(false)

  const {
    register,
    control,
    handleSubmit,
    setError,
    getValues,
    setValue,
    formState: { errors },
  } = useForm<EntradaPerfilTienda, unknown, DatosPerfilTienda>({
    resolver: zodResolver(esquemaPerfilTienda),
    mode: 'onTouched',
    shouldFocusError: true,
    defaultValues: perfilInicial(tienda),
  })
  const { fields: dias } = useFieldArray({ control, name: 'dias' })
  const diasAbiertos = useWatch({ control, name: 'dias' })

  const copiarLunes = () => {
    const [lunes] = getValues('dias')
    if (!lunes) return
    getValues('dias').forEach((dia, indice) => {
      if (indice > 0 && dia.abre) {
        setValue(`dias.${indice}.apertura`, lunes.apertura, { shouldValidate: true })
        setValue(`dias.${indice}.cierre`, lunes.cierre, { shouldValidate: true })
      }
    })
  }

  const enviar = (datos: DatosPerfilTienda) =>
    guardado.mutate(datos, {
      onSuccess: () => {
        setHuboCambios(true)
        anunciar(textos.cambiosGuardados)
      },
      onError: (error) => {
        if (!esErrorApi(error)) return
        error.errores.forEach(({ campo, mensaje }, indice) => {
          if (campo === 'nombre' || campo === 'telefono')
            setError(campo, { message: mensaje }, { shouldFocus: indice === 0 })
        })
      },
    })

  const error = guardado.error
  const errorDeCampo =
    esErrorApi(error) && error.errores.some(({ campo }) => campo === 'nombre' || campo === 'telefono')
  const errorGeneral =
    error && !errorDeCampo ? (esErrorApi(error) ? mensajeParaUsuario(error) : textos.errorApertura) : null

  return (
    <form
      noValidate
      onSubmit={handleSubmit(enviar)}
      onChange={() => setHuboCambios(false)}
      className="flex flex-col gap-6"
    >
      <CampoTexto
        etiqueta={textos.etiquetaNombreTienda}
        autoComplete="organization"
        error={errors.nombre?.message}
        {...register('nombre')}
      />
      <CampoTexto
        etiqueta={textos.etiquetaTelefono}
        pista={textos.pistaTelefonoTienda}
        type="tel"
        autoComplete="tel"
        inputMode="tel"
        error={errors.telefono?.message}
        {...register('telefono')}
      />

      <fieldset className="flex flex-col gap-3">
        <legend className="mb-1 font-semibold">{textos.etiquetaHorario}</legend>
        <p className="text-sm text-texto-suave">{textos.pistaHorario}</p>

        {errorGeneral ? (
          <div role="alert" className="rounded-control border border-peligro p-3 font-medium text-peligro">
            <p>{errorGeneral}</p>
            {esErrorApi(error) && debeMostrarIdCorrelacion(error) ? (
              <p className="mt-1 text-sm font-normal text-texto-suave">{textos.codigoSoporte(error.idCorrelacion)}</p>
            ) : null}
          </div>
        ) : null}

        <ul className="flex flex-col gap-2">
          {dias.map((dia, indice) => {
            const nombreDia = conMayuscula(textos.dias[dia.diaSemana])
            const abre = diasAbiertos[indice]?.abre ?? false
            const errorApertura = errors.dias?.[indice]?.apertura?.message
            const errorCierre = errors.dias?.[indice]?.cierre?.message
            return (
              <li
                key={dia.id}
                className={cn(
                  'flex flex-wrap items-center gap-x-4 gap-y-2 rounded-control border p-3',
                  abre ? 'border-borde' : 'border-borde-suave bg-superficie',
                )}
              >
                <label className="flex min-h-tactil min-w-28 cursor-pointer items-center gap-3 font-medium">
                  <input type="checkbox" className="size-6 accent-primario" {...register(`dias.${indice}.abre`)} />
                  {nombreDia}
                </label>
                {abre ? (
                  <div className="flex flex-wrap gap-3">
                    {(
                      [
                        ['apertura', textos.horaAbre, errorApertura],
                        ['cierre', textos.horaCierra, errorCierre],
                      ] as const
                    ).map(([campo, visible, mensaje]) => {
                      const idCampo = `horario-${dia.diaSemana}-${campo}`
                      return (
                        <div key={campo} className="flex flex-col gap-1">
                          <label htmlFor={idCampo} className="text-sm font-medium">
                            <span className="sr-only">{nombreDia}: </span>
                            {visible}
                          </label>
                          <input
                            id={idCampo}
                            type="time"
                            aria-invalid={mensaje ? true : undefined}
                            aria-describedby={mensaje ? `${idCampo}-error` : undefined}
                            className={cn(
                              'min-h-tactil rounded-control border border-borde bg-fondo px-2 text-base',
                              mensaje && 'border-peligro',
                            )}
                            {...register(`dias.${indice}.${campo}`)}
                          />
                          {mensaje ? (
                            <p id={`${idCampo}-error`} className="max-w-40 text-sm font-medium text-peligro">
                              {mensaje}
                            </p>
                          ) : null}
                        </div>
                      )
                    })}
                  </div>
                ) : (
                  <span className="text-texto-suave">{textos.diaCerrado}</span>
                )}
              </li>
            )
          })}
        </ul>
        <Boton type="button" variante="secundario" className="self-start" onClick={copiarLunes}>
          {textos.copiarLunes}
        </Boton>
      </fieldset>

      <div className="flex flex-col gap-2">
        <Boton type="submit" ancho="completo" enviando={guardado.isPending} textoEnviando={textos.botonGuardando}>
          {textos.botonGuardar}
        </Boton>
        {huboCambios && guardado.isSuccess ? <p className="font-medium text-exito">{textos.cambiosGuardados}</p> : null}
      </div>
    </form>
  )
}
