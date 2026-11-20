import { zodResolver } from '@hookform/resolvers/zod'
import { useCallback } from 'react'
import { useForm } from 'react-hook-form'
import { AvisoDeshacer } from '@/components/AvisoDeshacer'
import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { cn } from '@/components/ui/utilidades'
import { useDescartarConDeshacer } from '@/hooks/useDescartarConDeshacer'
import { esErrorApi } from '@/services/errores'
import {
  mensajeAdministracion,
  useAgregarRestringido,
  useQuitarRestringido,
  useRestringidos,
  type ProductoRestringido,
} from './api'
import { esquemaPalabraRestringida, type DatosPalabraRestringida } from './esquemas'
import { textosAdministracion as textos } from './textos'

const ID_CAMPO = 'restringido-palabra'

/**
 * HU-038, CP-CAT-038-02: lista, alta y baja de palabras restringidas (mockup 07). «Quitar» no pide confirmación: la
 * palabra sale de la lista y se puede deshacer durante 8 s (C-15, DECISIONES-FRONTEND §8); el DELETE se envía después.
 * El backend guarda la palabra normalizada, así que la lista muestra lo que quedó guardado.
 */
export function RestringidosAdmin() {
  const anunciar = useAnunciar()
  const restringidos = useRestringidos()
  const agregar = useAgregarRestringido()
  const quitar = useQuitarRestringido()
  const enviarQuitar = useCallback((palabra: ProductoRestringido) => quitar.mutate(palabra.id), [quitar])
  const { pendiente, descartar, deshacer, confirmar } = useDescartarConDeshacer(enviarQuitar)

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<DatosPalabraRestringida>({
    resolver: zodResolver(esquemaPalabraRestringida),
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    defaultValues: { palabraClave: '' },
  })

  const enviar = ({ palabraClave }: DatosPalabraRestringida) =>
    agregar.mutate(palabraClave, {
      onSuccess: (nueva) => {
        reset()
        anunciar(textos.agregada(nueva.palabraClave))
      },
      // Los errores de la palabra (CAT-002, CAT-003, validación) van en el campo
      onError: (error) => {
        if (!esErrorApi(error)) return
        const delCampo = error.errores.find(({ campo }) => campo === 'palabraClave')?.mensaje
        const mensaje =
          delCampo ?? (['CAT-002', 'CAT-003'].includes(error.codigo) ? mensajeAdministracion(error) : null)
        if (mensaje) setError('palabraClave', { message: mensaje }, { shouldFocus: true })
      },
    })

  const errorAgregar = agregar.error
  const errorGeneralAgregar = errorAgregar && !errors.palabraClave ? mensajeAdministracion(errorAgregar) : null
  const visibles = restringidos.data?.filter((palabra) => palabra.id !== pendiente?.id) ?? []

  return (
    <section aria-labelledby="catalogo-restringidos" className="flex flex-col gap-3">
      <h2 id="catalogo-restringidos" className="text-xl font-semibold">
        {textos.tituloRestringidos}
      </h2>
      <p className="text-texto-suave">{textos.pistaRestringidos}</p>

      {restringidos.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargandoRestringidos} />
      ) : restringidos.isError ? (
        <EstadoError error={restringidos.error} alReintentar={() => void restringidos.refetch()} />
      ) : visibles.length === 0 ? (
        <EstadoVacio titulo={textos.sinRestringidos}>{textos.sinRestringidosAyuda}</EstadoVacio>
      ) : (
        <ul className="flex flex-col">
          {visibles.map((palabra) => (
            <li key={palabra.id} className="flex items-center justify-between gap-3 border-b border-borde py-2">
              <span className="break-words capitalize">{palabra.palabraClave}</span>
              <Boton
                type="button"
                variante="secundario"
                aria-label={textos.quitarPalabra(palabra.palabraClave)}
                onClick={() => {
                  descartar(palabra)
                  anunciar(textos.quitasteAnuncio(palabra.palabraClave))
                }}
              >
                {textos.botonQuitar}
              </Boton>
            </li>
          ))}
        </ul>
      )}
      {quitar.error ? (
        <p role="alert" className="font-medium text-peligro">
          {mensajeAdministracion(quitar.error)}
        </p>
      ) : null}

      <form noValidate onSubmit={handleSubmit(enviar)} className="flex flex-col gap-1">
        <label htmlFor={ID_CAMPO} className="font-semibold">
          {textos.etiquetaAgregar}
        </label>
        <p id={`${ID_CAMPO}-pista`} className="text-sm text-texto-suave">
          {textos.pistaAgregar}
        </p>
        <div className="flex flex-wrap gap-2">
          <input
            id={ID_CAMPO}
            type="text"
            autoComplete="off"
            maxLength={120}
            aria-invalid={errors.palabraClave ? true : undefined}
            aria-describedby={[`${ID_CAMPO}-pista`, errors.palabraClave ? `${ID_CAMPO}-error` : null]
              .filter(Boolean)
              .join(' ')}
            className={cn(
              'min-h-tactil min-w-0 flex-1 rounded-control border border-borde bg-fondo px-3 text-base',
              errors.palabraClave && 'border-peligro',
            )}
            {...register('palabraClave')}
          />
          <Boton type="submit" enviando={agregar.isPending} textoEnviando={textos.agregando}>
            {textos.botonAgregar}
          </Boton>
        </div>
        {errors.palabraClave ? (
          <p id={`${ID_CAMPO}-error`} className="text-sm font-medium text-peligro">
            {errors.palabraClave.message}
          </p>
        ) : null}
        {errorGeneralAgregar ? (
          <p role="alert" className="font-medium text-peligro">
            {errorGeneralAgregar}
          </p>
        ) : null}
      </form>

      {pendiente ? (
        <AvisoDeshacer
          mensaje={textos.quitaste(pendiente.palabraClave)}
          alDeshacer={() => {
            const devuelta = deshacer()
            if (devuelta) anunciar(textos.restaurada(devuelta.palabraClave))
          }}
          alCerrar={confirmar}
        />
      ) : null}
    </section>
  )
}
