import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router'
import { CampoTexto } from '@/components/CampoTexto'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { esErrorApi } from '@/services/errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { verificarSegundoFactor, type Sesion } from '@/services/sesion'
import { esquemaSegundoFactor, LARGO_CODIGO_TOTP, type DatosSegundoFactor, type EntradaSegundoFactor } from './esquemas'
import { textosIdentidad as textos } from './textos'

/**
 * Código TOTP del administrador (HU-004, CU-25). Mismo patrón que FormularioInicioSesion.
 * - IDE-012 (código incorrecto): el error va al campo y el foco vuelve ahí para escribir el código nuevo.
 * - IDE-013 (venció o se agotaron los intentos): el token ya no sirve; se explica y se ofrece volver a entrar.
 */
export function FormularioSegundoFactor({ alVerificar }: { alVerificar: (sesion: Sesion) => void }) {
  const anunciar = useAnunciar()
  const {
    register,
    handleSubmit,
    setError,
    setValue,
    formState: { errors },
  } = useForm<EntradaSegundoFactor, unknown, DatosSegundoFactor>({
    resolver: zodResolver(esquemaSegundoFactor),
    mode: 'onTouched',
    shouldFocusError: true,
    defaultValues: { codigo: '' },
  })

  const verificacion = useMutation({
    mutationFn: ({ codigo }: DatosSegundoFactor) => verificarSegundoFactor(codigo),
    onSuccess: (sesion) => {
      anunciar(textos.sesionIniciada)
      alVerificar(sesion)
    },
    onError: (error) => {
      if (!esErrorApi(error)) return
      if (error.codigo === 'IDE-012') {
        // El código viejo ya no sirve: se borra para escribir el siguiente sin tener que seleccionarlo
        setValue('codigo', '')
        setError('codigo', { message: mensajeParaUsuario(error) }, { shouldFocus: true })
      } else if (error.esErrorDeValidacion) {
        const errorCodigo = error.errores.find(({ campo }) => campo === 'codigo')
        if (errorCodigo) setError('codigo', { message: errorCodigo.mensaje }, { shouldFocus: true })
      }
    },
  })

  const error = verificacion.error
  const vencio = esErrorApi(error) && error.codigo === 'IDE-013'
  const errorGeneral =
    error && !vencio && !(esErrorApi(error) && (error.codigo === 'IDE-012' || error.esErrorDeValidacion))
      ? esErrorApi(error)
        ? mensajeParaUsuario(error)
        : textos.errorGeneralSegundoFactor
      : null

  return (
    <form noValidate onSubmit={handleSubmit((datos) => verificacion.mutate(datos))} className="flex flex-col gap-4">
      {vencio ? (
        <div role="alert" className="flex flex-col gap-2 rounded-control border border-peligro p-3 font-medium">
          <p>{mensajeParaUsuario(error)}</p>
          <Link
            to="/entrar"
            replace
            className="inline-flex min-h-tactil items-center self-start text-primario underline underline-offset-4 hover:no-underline"
          >
            {textos.enlaceVolverAEntrar}
          </Link>
        </div>
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
        etiqueta={textos.etiquetaCodigo}
        pista={textos.pistaCodigo}
        inputMode="numeric"
        autoComplete="one-time-code"
        // Con espacios ("123 456") caben 7; el esquema los quita
        maxLength={LARGO_CODIGO_TOTP + 1}
        className="max-w-48 text-xl tracking-widest"
        disabled={vencio}
        error={errors.codigo?.message}
        {...register('codigo')}
      />

      <Boton
        type="submit"
        ancho="completo"
        disabled={vencio}
        enviando={verificacion.isPending}
        textoEnviando={textos.botonVerificando}
      >
        {textos.botonVerificar}
      </Boton>
    </form>
  )
}
