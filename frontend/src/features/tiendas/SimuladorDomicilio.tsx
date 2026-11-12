import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { CampoSeleccion } from '@/components/CampoSeleccion'
import { CampoTexto } from '@/components/CampoTexto'
import { Boton } from '@/components/ui/Boton'
import { esErrorApi } from '@/services/errores'
import { mensajeParaUsuario } from '@/services/mensajes-error'
import { formatearDistancia, formatearPesos } from '@/services/formato'
import { useSimularDomicilio, type Domicilio } from './api'
import { esquemaSimulacionDomicilio, type DatosSimulacionDomicilio, type EntradaSimulacionDomicilio } from './esquemas'
import { textosTiendas as textos } from './textos'

const DISTANCIAS_DE_PRUEBA_METROS = [300, 1000, 2000, 3000, 5000, 8000, 12_000]

/**
 * Prueba con el domicilio guardado cuánto pagaría un cliente (CU-17 pasos 7 a 10). Deshabilitado, con la razón visible,
 * mientras la tienda no tenga el domicilio activo y guardado.
 */
export function SimuladorDomicilio({ domicilioGuardado }: { domicilioGuardado: Domicilio }) {
  const simulacion = useSimularDomicilio()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<EntradaSimulacionDomicilio, unknown, DatosSimulacionDomicilio>({
    resolver: zodResolver(esquemaSimulacionDomicilio),
    mode: 'onTouched',
    defaultValues: { subtotal: '30000', distancia: '1000' },
  })

  const disponible = domicilioGuardado.haceDomicilio
  const error = simulacion.error
  const resultado = simulacion.isSuccess
    ? textos.resultadoSimulacion(formatearPesos(simulacion.data.costoDomicilioEnPesos))
    : esErrorApi(error) && error.codigo === 'TIE-010'
      ? textos.fueraDeCobertura
      : error
        ? esErrorApi(error)
          ? mensajeParaUsuario(error)
          : textos.errorApertura
        : null

  return (
    <section aria-labelledby="titulo-simulador" className="flex flex-col gap-4 rounded-control border border-borde p-4">
      <div className="flex flex-col gap-1">
        <h2 id="titulo-simulador" className="text-lg font-semibold">
          {textos.tituloSimulador}
        </h2>
        <p className="text-sm text-texto-suave">
          {disponible ? textos.pistaSimulador : textos.sinDomicilioParaSimular}
        </p>
      </div>
      {disponible ? (
        <form
          noValidate
          onSubmit={handleSubmit(({ subtotal, distancia }) =>
            simulacion.mutate({ subtotalEnPesos: subtotal, distanciaEnMetros: distancia }),
          )}
          className="flex flex-col gap-4"
        >
          <CampoTexto
            etiqueta={textos.etiquetaSubtotal}
            inputMode="numeric"
            autoComplete="off"
            error={errors.subtotal?.message}
            {...register('subtotal')}
          />
          <CampoSeleccion
            etiqueta={textos.etiquetaDistancia}
            opciones={DISTANCIAS_DE_PRUEBA_METROS.map((metros) => ({
              valor: String(metros),
              texto: formatearDistancia(metros),
            }))}
            error={errors.distancia?.message}
            {...register('distancia')}
          />
          <Boton
            type="submit"
            variante="secundario"
            className="self-start"
            enviando={simulacion.isPending}
            textoEnviando={textos.botonSimulando}
          >
            {textos.botonSimular}
          </Boton>
          <p role="status" className="font-medium">
            {resultado}
          </p>
        </form>
      ) : null}
    </section>
  )
}
