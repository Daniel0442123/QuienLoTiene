import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { CampoSeleccion } from '@/components/CampoSeleccion'
import { CampoTexto } from '@/components/CampoTexto'
import { CasillaVerificacion } from '@/components/CasillaVerificacion'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { esErrorApi } from '@/services/errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { formatearDistancia, formatearPesos } from '@/services/formato'
import { useGuardarDomicilio, type Domicilio } from './api'
import {
  esquemaDomicilio,
  pesosEscritos,
  RADIOS_COBERTURA_METROS,
  type DatosDomicilio,
  type EntradaDomicilio,
} from './esquemas'
import { textosTiendas as textos } from './textos'

function valoresIniciales(domicilio: Domicilio): EntradaDomicilio {
  return {
    haceDomicilio: domicilio.haceDomicilio,
    radio: domicilio.radioCoberturaEnMetros === null ? '' : String(domicilio.radioCoberturaEnMetros),
    tarifa: domicilio.tarifaFijaEnPesos === null ? '' : String(domicilio.tarifaFijaEnPesos),
  }
}

/**
 * Domicilio con tarifa fija (HU-010, CU-17 pasos 1 a 6). Activarlo muestra radio y valor; apagarlo los quita.
 * El valor se muestra como lo verán los clientes ("$ 4.000") mientras se escribe.
 */
export function FormularioDomicilio({ domicilio }: { domicilio: Domicilio }) {
  const anunciar = useAnunciar()
  const guardado = useGuardarDomicilio()
  const [huboCambios, setHuboCambios] = useState(false)

  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<EntradaDomicilio, unknown, DatosDomicilio>({
    resolver: zodResolver(esquemaDomicilio),
    mode: 'onTouched',
    shouldFocusError: true,
    defaultValues: valoresIniciales(domicilio),
  })
  const haceDomicilio = useWatch({ control, name: 'haceDomicilio' })
  const tarifaEscrita = pesosEscritos(useWatch({ control, name: 'tarifa' }))

  const radioGuardado = domicilio.radioCoberturaEnMetros
  const radios = [
    ...RADIOS_COBERTURA_METROS,
    ...(radioGuardado && !RADIOS_COBERTURA_METROS.some((r) => r === radioGuardado) ? [radioGuardado] : []),
  ]
    .sort((a, b) => a - b)
    .map((metros) => ({ valor: String(metros), texto: formatearDistancia(metros) }))

  const error = guardado.error
  const mensajeError = error ? (esErrorApi(error) ? mensajeParaUsuario(error) : textos.errorApertura) : null

  return (
    <form
      noValidate
      onSubmit={handleSubmit((datos) =>
        guardado.mutate(datos, {
          onSuccess: () => {
            setHuboCambios(true)
            anunciar(textos.cambiosGuardados)
          },
        }),
      )}
      onChange={() => setHuboCambios(false)}
      className="flex flex-col gap-5"
    >
      {mensajeError ? (
        <div role="alert" className="rounded-control border border-peligro p-3 font-medium text-peligro">
          <p>{mensajeError}</p>
          {esErrorApi(error) && debeMostrarIdCorrelacion(error) ? (
            <p className="mt-1 text-sm font-normal text-texto-suave">{textos.codigoSoporte(error.idCorrelacion)}</p>
          ) : null}
        </div>
      ) : null}

      <div className="flex flex-col gap-1">
        <CasillaVerificacion etiqueta={textos.etiquetaHaceDomicilio} {...register('haceDomicilio')} />
        <p className="text-sm text-texto-suave">{textos.pistaHaceDomicilio}</p>
      </div>

      {haceDomicilio ? (
        <>
          <CampoSeleccion
            etiqueta={textos.etiquetaRadio}
            opciones={[{ valor: '', texto: textos.elegirDistancia }, ...radios]}
            error={errors.radio?.message}
            {...register('radio')}
          />
          <CampoTexto
            etiqueta={textos.etiquetaTarifa}
            pista={
              <>
                {textos.pistaTarifa}
                {tarifaEscrita !== null ? (
                  <>
                    <br />
                    {textos.vistaTarifa(formatearPesos(tarifaEscrita))}
                  </>
                ) : null}
              </>
            }
            inputMode="numeric"
            autoComplete="off"
            error={errors.tarifa?.message}
            {...register('tarifa')}
          />
        </>
      ) : null}

      <div className="flex flex-col gap-2">
        <Boton type="submit" ancho="completo" enviando={guardado.isPending} textoEnviando={textos.botonGuardando}>
          {textos.botonGuardar}
        </Boton>
        {huboCambios && guardado.isSuccess ? <p className="font-medium text-exito">{textos.cambiosGuardados}</p> : null}
      </div>
    </form>
  )
}
