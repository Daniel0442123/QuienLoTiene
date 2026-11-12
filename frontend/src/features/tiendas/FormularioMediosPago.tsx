import { zodResolver } from '@hookform/resolvers/zod'
import { Banknote, CreditCard, Smartphone } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { cn } from '@/components/ui/utilidades'
import { esErrorApi } from '@/services/errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { useGuardarMediosPago, type MedioPago } from './api'
import { esquemaMediosPago, MEDIOS_PAGO, type DatosMediosPago } from './esquemas'
import { textosTiendas as textos } from './textos'

const ICONOS = { EFECTIVO: Banknote, TRANSFERENCIA: Smartphone, DATAFONO: CreditCard }

/**
 * Medios de pago que acepta la tienda (HU-012, CU-18). Al menos uno: se revisa antes de enviar y el backend lo repite
 * (TIE-011). Al guardar, el medio que no queda marcado deja de aparecer en las órdenes nuevas (CP-TIE-012-02).
 */
export function FormularioMediosPago({ mediosPago }: { mediosPago: MedioPago[] }) {
  const anunciar = useAnunciar()
  const guardado = useGuardarMediosPago()
  const [huboCambios, setHuboCambios] = useState(false)
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<DatosMediosPago>({
    resolver: zodResolver(esquemaMediosPago),
    mode: 'onTouched',
    defaultValues: { mediosPago },
  })

  const error = guardado.error
  const mensajeError = error ? (esErrorApi(error) ? mensajeParaUsuario(error) : textos.errorApertura) : null
  const errorDeCampo = errors.mediosPago?.message ?? errors.mediosPago?.root?.message

  return (
    <form
      noValidate
      onSubmit={handleSubmit(
        (datos) =>
          guardado.mutate(datos.mediosPago, {
            onSuccess: () => {
              setHuboCambios(true)
              anunciar(textos.mediosGuardados)
            },
          }),
        // Sin ningún medio marcado, el foco va a la primera casilla para corregir
        () => document.querySelector<HTMLInputElement>('input[name="mediosPago"]')?.focus(),
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

      <fieldset
        className="flex flex-col gap-2"
        aria-describedby={['medios-pista', errorDeCampo ? 'medios-error' : null].filter(Boolean).join(' ')}
      >
        <legend className="mb-1 font-semibold">{textos.etiquetaMediosPago}</legend>
        <p id="medios-pista" className="text-sm text-texto-suave">
          {textos.pistaMediosPago}
        </p>
        {MEDIOS_PAGO.map((medio) => {
          const Icono = ICONOS[medio]
          return (
            <label
              key={medio}
              className={cn(
                'flex min-h-tactil cursor-pointer items-center gap-3 rounded-control border p-3 has-checked:border-primario has-checked:bg-superficie',
                errorDeCampo ? 'border-peligro' : 'border-borde',
              )}
            >
              <input type="checkbox" value={medio} className="size-6 accent-primario" {...register('mediosPago')} />
              <Icono aria-hidden="true" className="size-5 shrink-0 text-texto-suave" />
              <span>{textos.mediosPago[medio]}</span>
            </label>
          )
        })}
        {errorDeCampo ? (
          <p id="medios-error" className="text-sm font-medium text-peligro">
            {errorDeCampo}
          </p>
        ) : null}
      </fieldset>

      <div className="flex flex-col gap-2">
        <Boton type="submit" ancho="completo" enviando={guardado.isPending} textoEnviando={textos.botonGuardando}>
          {textos.botonGuardar}
        </Boton>
        {huboCambios && guardado.isSuccess ? <p className="font-medium text-exito">{textos.mediosGuardados}</p> : null}
      </div>
    </form>
  )
}
