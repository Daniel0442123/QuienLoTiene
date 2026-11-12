import { zodResolver } from '@hookform/resolvers/zod'
import { useState, type ReactNode } from 'react'
import { Controller, useForm, useWatch, type FieldPath } from 'react-hook-form'
import { Link } from 'react-router'
import { CampoArchivo } from '@/components/CampoArchivo'
import { CampoTexto } from '@/components/CampoTexto'
import { CasillaVerificacion } from '@/components/CasillaVerificacion'
import { EstadoError, EstadoVacio } from '@/components/Estados'
import { PoliticaDatosDesplegable } from '@/components/PoliticaDatosDesplegable'
import { enfocarCampo } from '@/components/enfocarCampo'
import { ResumenErrores } from '@/components/ResumenErrores'
import { useAnunciar } from '@/components/RegionAnuncios'
import { SelectorUbicacion } from '@/components/SelectorUbicacion'
import { Boton } from '@/components/ui/Boton'
import { useCategorias } from '@/hooks/useCategorias'
import { problemaDeArchivo, reducirFoto, TIPOS_DOCUMENTO_PERMITIDOS } from '@/services/archivos'
import { esErrorApi, type ErrorApi } from '@/services/errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { useRegistrarTienda } from './api'
import { esquemaRegistroTienda, type DatosRegistroTienda, type EntradaRegistroTienda } from './esquemas'
import { textosTiendas as textos } from './textos'

type Campo = FieldPath<EntradaRegistroTienda>
type CampoPrincipal = Exclude<Campo, `ubicacion.${string}`>

/** id de cada campo en el orden en que aparecen: el resumen de errores enlaza a ellos. */
const ID_CAMPO = {
  nombreDueno: 'tienda-dueno',
  correo: 'tienda-correo',
  telefono: 'tienda-telefono',
  fraseContrasena: 'tienda-frase',
  nombreTienda: 'tienda-nombre',
  categoriaId: 'tienda-categoria',
  ubicacion: 'tienda-ubicacion',
  fotoLocal: 'tienda-foto',
  rut: 'tienda-rut',
  aceptaPoliticaDatos: 'tienda-politica',
} as const satisfies Record<CampoPrincipal, string>
const CAMPOS = Object.keys(ID_CAMPO) as CampoPrincipal[]

/** Errores del backend que corresponden a un campo (CODIGOS-DE-ERROR.md). */
const CAMPO_POR_CODIGO: Record<string, CampoPrincipal> = {
  'IDE-001': 'correo',
  'IDE-002': 'fraseContrasena',
  'IDE-003': 'fraseContrasena',
  'IDE-004': 'fraseContrasena',
  'IDE-005': 'correo',
  'IDE-006': 'aceptaPoliticaDatos',
}

/** "ubicacion.latitud" → "ubicacion"; los demás campos llegan con su nombre. */
function campoDelBackend(campo: string): CampoPrincipal | null {
  const principal = campo.split('.')[0] ?? ''
  return principal in ID_CAMPO ? (principal as CampoPrincipal) : null
}

/** TIE-002 dice en el detalle qué documento falta: se marca ese campo. */
function campoDeDocumentoFaltante(error: ErrorApi): CampoPrincipal | null {
  if (error.codigo !== 'TIE-002') return null
  return /foto/i.test(error.detalle ?? '') ? 'fotoLocal' : 'rut'
}

function Seccion({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <fieldset className="flex flex-col gap-5">
      <legend className="mb-3 text-lg font-semibold">{titulo}</legend>
      {children}
    </fieldset>
  )
}

/**
 * Registro de tienda con documentos y autorización de datos (HU-002, HU-005, CU-15). Sigue el patrón de
 * FormularioRegistroCliente: valida al salir del campo, resume los errores arriba y lleva el foco al primero.
 */
export function FormularioRegistroTienda({ alRegistrar }: { alRegistrar: (nombreTienda: string) => void }) {
  const anunciar = useAnunciar()
  const categorias = useCategorias()
  const registro = useRegistrarTienda()
  const [fraseVisible, setFraseVisible] = useState(false)
  const [reduciendo, setReduciendo] = useState<'fotoLocal' | 'rut' | null>(null)

  const {
    register,
    control,
    handleSubmit,
    setError,
    clearErrors,
    setFocus,
    trigger,
    formState: { errors, isSubmitted },
  } = useForm<EntradaRegistroTienda, unknown, DatosRegistroTienda>({
    resolver: zodResolver(esquemaRegistroTienda),
    mode: 'onTouched',
    shouldFocusError: false, // el foco lo maneja `enfocarPrimerError`: el mapa y los archivos no son inputs comunes
    defaultValues: {
      nombreDueno: '',
      correo: '',
      telefono: '',
      fraseContrasena: '',
      nombreTienda: '',
      categoriaId: '',
      ubicacion: null,
      fotoLocal: null,
      rut: null,
      aceptaPoliticaDatos: false,
    },
  })

  const largoFrase = useWatch({ control, name: 'fraseContrasena' }).length

  const enfocarPrimerError = (campos: CampoPrincipal[]) => {
    const primero = CAMPOS.find((campo) => campos.includes(campo))
    if (primero && !enfocarCampo(ID_CAMPO[primero])) setFocus(primero)
  }

  /** Las fotos se reducen antes de guardarlas en el formulario; el esquema revisa el tipo y el tamaño final. */
  const elegirArchivo = async (
    campo: 'fotoLocal' | 'rut',
    archivo: File | null,
    alCambiar: (valor: File | null) => void,
  ) => {
    let preparado = archivo
    if (archivo && problemaDeArchivo(archivo) !== 'tipo') {
      setReduciendo(campo)
      preparado = await reducirFoto(archivo)
      setReduciendo(null)
    }
    alCambiar(preparado)
    await trigger(campo)
  }

  const enviar = (datos: DatosRegistroTienda) =>
    registro.mutate(datos, {
      onSuccess: (tienda) => {
        anunciar(textos.tiendaRegistrada)
        alRegistrar(tienda.nombre)
      },
      onError: (error) => {
        if (!esErrorApi(error)) return
        const marcados: Array<{ campo: CampoPrincipal; mensaje: string }> = []
        error.errores.forEach(({ campo, mensaje }) => {
          const principal = campoDelBackend(campo)
          if (principal) marcados.push({ campo: principal, mensaje })
        })
        const campoDelCodigo = CAMPO_POR_CODIGO[error.codigo] ?? campoDeDocumentoFaltante(error)
        if (campoDelCodigo) {
          const mensaje = error.codigo === 'IDE-001' ? textos.correoYaRegistrado : mensajeParaUsuario(error)
          marcados.push({ campo: campoDelCodigo, mensaje })
        }
        marcados.forEach(({ campo, mensaje }) => setError(campo, { message: mensaje }))
        enfocarPrimerError(marcados.map(({ campo }) => campo))
      },
    })

  const error = registro.error
  const marcadoEnCampo =
    esErrorApi(error) && (error.esErrorDeValidacion || error.codigo in CAMPO_POR_CODIGO || error.codigo === 'TIE-002')
  const mensajeCorreoRepetido = esErrorApi(error) && error.codigo === 'IDE-001' ? mensajeParaUsuario(error) : null
  const errorGeneral =
    error && !marcadoEnCampo ? (esErrorApi(error) ? mensajeParaUsuario(error) : textos.errorGeneralRegistro) : null

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
      className="flex flex-col gap-8"
    >
      <ResumenErrores titulo={textos.tituloResumenErrores} errores={erroresResumen} />

      {mensajeCorreoRepetido ? (
        <p role="alert" className="rounded-control border border-peligro p-3 font-medium">
          {mensajeCorreoRepetido}{' '}
          <Link to="/entrar" className="text-primario underline underline-offset-4 hover:no-underline">
            {textos.enlaceEntrarConCorreo}
          </Link>
        </p>
      ) : null}

      {errorGeneral ? (
        <div role="alert" className="rounded-control border border-peligro p-3 font-medium text-peligro">
          <p>{errorGeneral}</p>
          {esErrorApi(error) && debeMostrarIdCorrelacion(error) ? (
            <p className="mt-1 text-sm font-normal text-texto-suave">{textos.codigoSoporte(error.idCorrelacion)}</p>
          ) : null}
        </div>
      ) : null}

      <Seccion titulo={textos.seccionDueno}>
        <CampoTexto
          id={ID_CAMPO.nombreDueno}
          etiqueta={textos.etiquetaNombreDueno}
          autoComplete="name"
          error={errors.nombreDueno?.message}
          {...register('nombreDueno')}
        />
        <CampoTexto
          id={ID_CAMPO.correo}
          etiqueta={textos.etiquetaCorreo}
          type="email"
          autoComplete="email"
          inputMode="email"
          error={errors.correo?.message}
          {...register('correo')}
        />
        <CampoTexto
          id={ID_CAMPO.telefono}
          etiqueta={textos.etiquetaTelefono}
          pista={textos.pistaTelefono}
          type="tel"
          autoComplete="tel"
          inputMode="tel"
          error={errors.telefono?.message}
          {...register('telefono')}
        />
        <div className="flex flex-col gap-2">
          <CampoTexto
            id={ID_CAMPO.fraseContrasena}
            etiqueta={textos.etiquetaFrase}
            pista={textos.pistaFrase(largoFrase)}
            type={fraseVisible ? 'text' : 'password'}
            autoComplete="new-password"
            error={errors.fraseContrasena?.message}
            {...register('fraseContrasena')}
          />
          <Boton
            type="button"
            variante="enlace"
            className="self-start px-0"
            aria-pressed={fraseVisible}
            onClick={() => setFraseVisible((visible) => !visible)}
          >
            {fraseVisible ? textos.ocultarFrase : textos.mostrarFrase}
          </Boton>
        </div>
      </Seccion>

      <Seccion titulo={textos.seccionTienda}>
        <CampoTexto
          id={ID_CAMPO.nombreTienda}
          etiqueta={textos.etiquetaNombreTienda}
          pista={textos.pistaNombreTienda}
          autoComplete="organization"
          error={errors.nombreTienda?.message}
          {...register('nombreTienda')}
        />

        <fieldset
          id={ID_CAMPO.categoriaId}
          className="flex flex-col gap-2"
          aria-describedby={errors.categoriaId ? `${ID_CAMPO.categoriaId}-error` : undefined}
        >
          <legend className="mb-1 font-medium">{textos.etiquetaCategoria}</legend>
          {categorias.isPending ? (
            <div role="status" aria-label={textos.cargandoCategorias} className="flex flex-col gap-2">
              <div className="h-tactil animate-pulse rounded-control bg-borde-suave" />
              <div className="h-tactil animate-pulse rounded-control bg-borde-suave" />
            </div>
          ) : categorias.isError ? (
            <EstadoError error={categorias.error} alReintentar={() => void categorias.refetch()} />
          ) : categorias.data.length === 0 ? (
            <EstadoVacio titulo={textos.sinCategorias}>{textos.sinCategoriasAyuda}</EstadoVacio>
          ) : (
            <div className="flex flex-col gap-2 sm:flex-row">
              {categorias.data.map((categoria) => (
                <label
                  key={categoria.id}
                  className="flex min-h-tactil flex-1 cursor-pointer items-center gap-3 rounded-control border border-borde px-3 has-checked:border-primario has-checked:bg-superficie has-checked:font-semibold"
                >
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
            <SelectorUbicacion
              id={ID_CAMPO.ubicacion}
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
      </Seccion>

      <Seccion titulo={textos.seccionDocumentos}>
        <p className="-mt-2 text-texto-suave">{textos.introDocumentos}</p>
        {(['fotoLocal', 'rut'] as const).map((campo) => (
          <Controller
            key={campo}
            control={control}
            name={campo}
            render={({ field }) => (
              <CampoArchivo
                id={ID_CAMPO[campo]}
                name={field.name}
                etiqueta={campo === 'fotoLocal' ? textos.etiquetaFotoLocal : textos.etiquetaRut}
                pista={campo === 'fotoLocal' ? textos.pistaFotoLocal : textos.pistaRut}
                accept={
                  campo === 'fotoLocal' ? 'image/jpeg,image/png,image/webp' : TIPOS_DOCUMENTO_PERMITIDOS.join(',')
                }
                archivo={field.value ?? null}
                alElegir={(archivo) => void elegirArchivo(campo, archivo, field.onChange)}
                onBlur={field.onBlur}
                procesando={reduciendo === campo}
                textoProcesando={textos.reduciendoFoto}
                error={errors[campo]?.message}
              />
            )}
          />
        ))}
      </Seccion>

      <div className="flex flex-col gap-2">
        <PoliticaDatosDesplegable />
        <CasillaVerificacion
          id={ID_CAMPO.aceptaPoliticaDatos}
          etiqueta={textos.etiquetaPolitica}
          error={errors.aceptaPoliticaDatos?.message}
          {...register('aceptaPoliticaDatos')}
        />
      </div>

      <Boton
        type="submit"
        ancho="completo"
        disabled={reduciendo !== null}
        enviando={registro.isPending}
        textoEnviando={textos.botonRegistrando}
      >
        {textos.botonRegistrar}
      </Boton>
    </form>
  )
}
