import { zodResolver } from '@hookform/resolvers/zod'
import { useCallback, useEffect, useRef, useState } from 'react'
import { Controller, useForm, type FieldPath } from 'react-hook-form'
import { CasillaVerificacion } from '@/components/CasillaVerificacion'
import { enfocarCampo } from '@/components/enfocarCampo'
import { EstadoError, EstadoVacio } from '@/components/Estados'
import { ResumenErrores } from '@/components/ResumenErrores'
import { Boton } from '@/components/ui/Boton'
import { cn } from '@/components/ui/utilidades'
import { useCategorias } from '@/hooks/useCategorias'
import { esErrorApi } from '@/services/errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { usePublicarSolicitud, type Solicitud } from './api'
import {
  esquemaPublicarSolicitud,
  MODALIDADES,
  type DatosPublicarSolicitud,
  type EntradaPublicarSolicitud,
} from './esquemas'
import { textosSolicitudes as textos } from './textos'
import { UbicacionSolicitud } from './UbicacionSolicitud'

type Campo = FieldPath<EntradaPublicarSolicitud>
type CampoPrincipal = Exclude<Campo, `ubicacion.${string}`>

const ID_CAMPO = {
  producto: 'solicitud-producto',
  categoriaId: 'solicitud-categoria',
  ubicacion: 'solicitud-ubicacion',
  modalidad: 'solicitud-modalidad',
  cantidad: 'solicitud-cantidad',
  esUrgente: 'solicitud-urgente',
} as const satisfies Record<CampoPrincipal, string>
const CAMPOS = Object.keys(ID_CAMPO) as CampoPrincipal[]
const ID_TITULO = 'solicitud-titulo'

function campoDelBackend(campo: string): CampoPrincipal | null {
  const principal = campo.split('.')[0] ?? ''
  return principal in ID_CAMPO ? (principal as CampoPrincipal) : null
}

const claseOpcion =
  'flex min-h-tactil flex-1 cursor-pointer items-center gap-3 rounded-control border border-borde px-3 has-checked:border-primario has-checked:bg-superficie has-checked:font-semibold'

/**
 * Publicar «¿quién tiene X?» (HU-016). Diseñado para RNF-027 (≤ 60 s): el título es la etiqueta del único campo
 * que hay que escribir, que recibe el foco al entrar; la ubicación viene de la primera dirección guardada, la modalidad
 * arranca en «recoger» y la cantidad y la urgencia quedan plegadas en «Más detalles».
 */
export function FormularioPublicarSolicitud({ alPublicar }: { alPublicar: (solicitud: Solicitud) => void }) {
  const categorias = useCategorias()
  const publicacion = usePublicarSolicitud()
  const campoProducto = useRef<HTMLInputElement | null>(null)
  const [detallesAbiertos, setDetallesAbiertos] = useState(false)

  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitted },
  } = useForm<EntradaPublicarSolicitud, unknown, DatosPublicarSolicitud>({
    resolver: zodResolver(esquemaPublicarSolicitud),
    mode: 'onTouched',
    shouldFocusError: false,
    defaultValues: {
      producto: '',
      categoriaId: '',
      modalidad: 'RECOGER',
      cantidad: '1',
      esUrgente: false,
      ubicacion: null,
    },
  })
  const { ref: refProducto, ...registroProducto } = register('producto')

  // Foco en el campo al entrar (DECISIONES-FRONTEND 8). Después del AnunciadorDeRuta, que enfoca el h1: el h1 es la
  // etiqueta de este campo, así que el lector de pantalla lee la misma pregunta.
  useEffect(() => {
    const cuadro = requestAnimationFrame(() => campoProducto.current?.focus())
    return () => cancelAnimationFrame(cuadro)
  }, [])

  const enfocarPrimerError = useCallback((campos: CampoPrincipal[]) => {
    const primero = CAMPOS.find((campo) => campos.includes(campo))
    if (!primero) return
    if (primero === 'cantidad' || primero === 'esUrgente') setDetallesAbiertos(true)
    // Si el campo estaba plegado, se espera a que se despliegue para enfocarlo
    requestAnimationFrame(() => enfocarCampo(ID_CAMPO[primero]))
  }, [])

  const enviar = (datos: DatosPublicarSolicitud) =>
    publicacion.mutate(datos, {
      onSuccess: alPublicar,
      onError: (error) => {
        if (!esErrorApi(error)) return
        const marcados = error.errores.flatMap(({ campo, mensaje }) => {
          const principal = campoDelBackend(campo)
          return principal ? [{ campo: principal, mensaje }] : []
        })
        marcados.forEach(({ campo, mensaje }) => setError(campo, { message: mensaje }))
        enfocarPrimerError(marcados.map(({ campo }) => campo))
      },
    })

  const error = publicacion.error
  const marcadoEnCampo = esErrorApi(error) && error.esErrorDeValidacion
  const errorGeneral =
    error && !marcadoEnCampo ? (esErrorApi(error) ? mensajeParaUsuario(error) : textos.errorGeneral) : null

  const erroresResumen = isSubmitted
    ? CAMPOS.flatMap((campo) => {
        const mensaje = errors[campo]?.message
        return mensaje ? [{ idCampo: ID_CAMPO[campo], mensaje }] : []
      })
    : []

  return (
    <form
      noValidate
      onSubmit={handleSubmit(enviar, (fallas) => enfocarPrimerError(Object.keys(fallas) as CampoPrincipal[]))}
      className="flex flex-col gap-5"
    >
      <div className="flex flex-col gap-1">
        <h1 id={ID_TITULO} className="text-2xl font-bold">
          {textos.tituloPublicar}
        </h1>
        <p id={`${ID_CAMPO.producto}-pista`} className="text-sm text-texto-suave">
          {textos.pistaProducto}
        </p>
        <input
          id={ID_CAMPO.producto}
          type="text"
          enterKeyHint="send"
          autoComplete="off"
          aria-labelledby={ID_TITULO}
          aria-invalid={errors.producto ? true : undefined}
          aria-describedby={[`${ID_CAMPO.producto}-pista`, errors.producto ? `${ID_CAMPO.producto}-error` : null]
            .filter(Boolean)
            .join(' ')}
          className={cn(
            'min-h-tactil rounded-control border border-borde bg-fondo px-3 text-lg',
            errors.producto && 'border-peligro',
          )}
          ref={(elemento) => {
            refProducto(elemento)
            campoProducto.current = elemento
          }}
          {...registroProducto}
        />
        {errors.producto ? (
          <p id={`${ID_CAMPO.producto}-error`} className="text-sm font-medium text-peligro">
            {errors.producto.message}
          </p>
        ) : null}
      </div>

      <ResumenErrores titulo={textos.tituloResumenErrores} errores={erroresResumen} />

      {errorGeneral ? (
        <div role="alert" className="rounded-control border border-peligro p-3 font-medium text-peligro">
          <p>{errorGeneral}</p>
          {esErrorApi(error) && debeMostrarIdCorrelacion(error) ? (
            <p className="mt-1 text-sm font-normal text-texto-suave">{textos.codigoSoporte(error.idCorrelacion)}</p>
          ) : null}
        </div>
      ) : null}

      <fieldset
        id={ID_CAMPO.categoriaId}
        className="flex flex-col gap-2"
        aria-describedby={errors.categoriaId ? `${ID_CAMPO.categoriaId}-error` : undefined}
      >
        <legend className="mb-1 font-semibold">{textos.etiquetaCategoria}</legend>
        {categorias.isPending ? (
          <div
            role="status"
            aria-label={textos.cargandoCategorias}
            className="h-tactil animate-pulse rounded-control bg-borde-suave"
          />
        ) : categorias.isError ? (
          <EstadoError error={categorias.error} alReintentar={() => void categorias.refetch()} />
        ) : categorias.data.length === 0 ? (
          <EstadoVacio titulo={textos.sinCategorias}>{textos.sinCategoriasAyuda}</EstadoVacio>
        ) : (
          <div className="flex flex-wrap gap-2">
            {categorias.data.map((categoria) => (
              <label key={categoria.id} className={claseOpcion}>
                <input
                  type="radio"
                  value={categoria.id}
                  className="size-5 accent-primario"
                  {...register('categoriaId')}
                />
                {categoria.nombre}
              </label>
            ))}
          </div>
        )}
        {errors.categoriaId ? (
          <p id={`${ID_CAMPO.categoriaId}-error`} className="text-sm font-medium text-peligro">
            {errors.categoriaId.message}
          </p>
        ) : null}
      </fieldset>

      <Controller
        control={control}
        name="ubicacion"
        render={({ field }) => (
          <UbicacionSolicitud
            id={ID_CAMPO.ubicacion}
            valor={field.value}
            alCambiar={field.onChange}
            error={errors.ubicacion?.message}
          />
        )}
      />

      <fieldset id={ID_CAMPO.modalidad} className="flex flex-col gap-2">
        <legend className="mb-1 font-semibold">{textos.etiquetaModalidad}</legend>
        <div className="flex flex-wrap gap-2">
          {MODALIDADES.map((modalidad) => (
            <label key={modalidad} className={claseOpcion}>
              <input type="radio" value={modalidad} className="size-5 accent-primario" {...register('modalidad')} />
              {textos.modalidades[modalidad]}
            </label>
          ))}
        </div>
      </fieldset>

      <details
        open={detallesAbiertos}
        onToggle={(evento) => setDetallesAbiertos(evento.currentTarget.open)}
        className="rounded-control border border-borde-suave"
      >
        <summary className="flex min-h-tactil cursor-pointer items-center px-3 font-medium text-primario underline underline-offset-4">
          {textos.masDetalles}
        </summary>
        <div className="flex flex-col gap-4 px-3 pb-3">
          <div className="flex flex-col gap-1">
            <label htmlFor={ID_CAMPO.cantidad} className="font-medium">
              {textos.etiquetaCantidad}
            </label>
            <p id={`${ID_CAMPO.cantidad}-pista`} className="text-sm text-texto-suave">
              {textos.pistaCantidad}
            </p>
            <input
              id={ID_CAMPO.cantidad}
              type="text"
              inputMode="numeric"
              autoComplete="off"
              aria-invalid={errors.cantidad ? true : undefined}
              aria-describedby={[`${ID_CAMPO.cantidad}-pista`, errors.cantidad ? `${ID_CAMPO.cantidad}-error` : null]
                .filter(Boolean)
                .join(' ')}
              className={cn(
                'min-h-tactil w-28 rounded-control border border-borde bg-fondo px-3 text-base',
                errors.cantidad && 'border-peligro',
              )}
              {...register('cantidad')}
            />
            {errors.cantidad ? (
              <p id={`${ID_CAMPO.cantidad}-error`} className="text-sm font-medium text-peligro">
                {errors.cantidad.message}
              </p>
            ) : null}
          </div>
          <CasillaVerificacion id={ID_CAMPO.esUrgente} etiqueta={textos.etiquetaUrgente} {...register('esUrgente')} />
        </div>
      </details>

      {/* Fijo abajo para que «Preguntar» siempre se vea sin desplazarse (RNF-027). Mide 4rem, el mismo
          --alto-barra-inferior que usa scroll-padding-bottom para que no tape el campo enfocado (WCAG 2.4.11) */}
      <div className="sticky bottom-0 -mx-4 bg-fondo px-4 py-2.5 shadow-[0_-1px_0_var(--color-borde-suave)]">
        <Boton type="submit" ancho="completo" enviando={publicacion.isPending} textoEnviando={textos.botonPreguntando}>
          {textos.botonPreguntar}
        </Boton>
      </div>
    </form>
  )
}
