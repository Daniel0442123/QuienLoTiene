import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm, useWatch, type FieldPath } from 'react-hook-form'
import { Link } from 'react-router'
import { CampoTexto } from '@/components/CampoTexto'
import { CasillaVerificacion } from '@/components/CasillaVerificacion'
import { PoliticaDatosDesplegable } from '@/components/PoliticaDatosDesplegable'
import { ResumenErrores } from '@/components/ResumenErrores'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { esErrorApi, type ErrorApi } from '@/services/errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { useRegistrarCliente } from './api'
import { esquemaRegistroCliente, type DatosRegistroCliente, type EntradaRegistroCliente } from './esquemas'
import { textosIdentidad as textos } from './textos'

type Campo = FieldPath<EntradaRegistroCliente>

/** id de cada campo, en el orden en que aparecen: el resumen de errores enlaza a ellos. */
const ID_CAMPO: Record<Campo, string> = {
  nombre: 'registro-nombre',
  correo: 'registro-correo',
  telefono: 'registro-telefono',
  fraseContrasena: 'registro-frase',
  aceptaPoliticaDatos: 'registro-politica',
}
const CAMPOS = Object.keys(ID_CAMPO) as Campo[]

/** Errores del backend que corresponden a un campo (CODIGOS-DE-ERROR.md). */
const CAMPO_POR_CODIGO: Record<string, Campo> = {
  'IDE-001': 'correo',
  'IDE-002': 'fraseContrasena',
  'IDE-003': 'fraseContrasena',
  'IDE-004': 'fraseContrasena',
  'IDE-005': 'correo',
  'IDE-006': 'aceptaPoliticaDatos',
}

function esCampo(nombre: string): nombre is Campo {
  return nombre in ID_CAMPO
}

/** El error se muestra arriba del formulario solo si no se pudo ubicar en un campo. */
function esErrorDeCampo(error: ErrorApi): boolean {
  return error.errores.some(({ campo }) => esCampo(campo)) || error.codigo in CAMPO_POR_CODIGO
}

/**
 * Registro de cliente con autorización de datos (HU-001, HU-005, CU-01). Sigue el patrón de FormularioInicioSesion:
 * valida al salir del campo, resume los errores arriba y lleva el foco al primero; los errores del backend van a su campo.
 */
export function FormularioRegistroCliente({ alRegistrar }: { alRegistrar: (correo: string) => void }) {
  const anunciar = useAnunciar()
  const [fraseVisible, setFraseVisible] = useState(false)
  const {
    register,
    handleSubmit,
    setError,
    control,
    formState: { errors, isSubmitted },
  } = useForm<EntradaRegistroCliente, unknown, DatosRegistroCliente>({
    resolver: zodResolver(esquemaRegistroCliente),
    mode: 'onTouched',
    shouldFocusError: true,
    defaultValues: { nombre: '', correo: '', telefono: '', fraseContrasena: '', aceptaPoliticaDatos: false },
  })

  const registro = useRegistrarCliente()

  const enviar = (datos: DatosRegistroCliente) =>
    registro.mutate(datos, {
      onSuccess: () => {
        anunciar(textos.cuentaCreada)
        alRegistrar(datos.correo)
      },
      onError: (error) => {
        if (!esErrorApi(error)) return
        const errores = error.errores.filter(({ campo }) => esCampo(campo))
        const campoDelCodigo = CAMPO_POR_CODIGO[error.codigo]
        if (campoDelCodigo) {
          const mensaje = error.codigo === 'IDE-001' ? textos.correoYaRegistrado : mensajeParaUsuario(error)
          errores.push({ campo: campoDelCodigo, mensaje })
        }
        errores
          .toSorted((a, b) => CAMPOS.indexOf(a.campo as Campo) - CAMPOS.indexOf(b.campo as Campo))
          .forEach(({ campo, mensaje }, indice) => {
            setError(campo as Campo, { message: mensaje }, { shouldFocus: indice === 0 })
          })
      },
    })

  const error = registro.error
  const mensajeCorreoRepetido = esErrorApi(error) && error.codigo === 'IDE-001' ? mensajeParaUsuario(error) : null
  const errorGeneral =
    error && !(esErrorApi(error) && esErrorDeCampo(error))
      ? esErrorApi(error)
        ? mensajeParaUsuario(error)
        : textos.errorGeneralRegistro
      : null

  const erroresResumen = isSubmitted
    ? CAMPOS.flatMap((campo) => {
        const mensaje = errors[campo]?.message
        return mensaje ? [{ idCampo: ID_CAMPO[campo], mensaje }] : []
      })
    : []

  const largoFrase = useWatch({ control, name: 'fraseContrasena' }).length

  return (
    <form noValidate onSubmit={handleSubmit(enviar)} className="flex flex-col gap-5">
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

      <CampoTexto
        id={ID_CAMPO.nombre}
        etiqueta={textos.etiquetaNombre}
        pista={textos.pistaNombre}
        autoComplete="name"
        error={errors.nombre?.message}
        {...register('nombre')}
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
          pista={textos.pistaFraseNueva(largoFrase)}
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

      <div className="flex flex-col gap-2">
        <PoliticaDatosDesplegable />
        <CasillaVerificacion
          id={ID_CAMPO.aceptaPoliticaDatos}
          etiqueta={textos.etiquetaPolitica}
          error={errors.aceptaPoliticaDatos?.message}
          {...register('aceptaPoliticaDatos')}
        />
      </div>

      <Boton type="submit" ancho="completo" enviando={registro.isPending} textoEnviando={textos.botonCreandoCuenta}>
        {textos.botonCrearCuenta}
      </Boton>
    </form>
  )
}
