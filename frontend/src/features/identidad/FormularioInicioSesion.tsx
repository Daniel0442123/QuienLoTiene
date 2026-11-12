import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { CampoTexto } from '@/components/CampoTexto'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { esErrorApi } from '@/services/errores'
import { mensajeParaUsuario } from '@/services/mensajes-error'
import { iniciarSesion, type RespuestaInicioSesion } from '@/services/sesion'
import { esquemaInicioSesion, type DatosInicioSesion } from './esquemas'
import { textosIdentidad as textos } from './textos'

/**
 * Ejemplo de referencia del patrón de formularios (DECISIONES-FRONTEND sección 9). HU-003.
 * Valida al salir del campo, asocia los errores con aria-describedby, permite pegar la frase y usar el gestor de
 * contraseñas (WCAG 3.3.8) y lleva el foco al primer campo con error.
 */
export function FormularioInicioSesion({
  alIniciar,
  correoInicial = '',
}: {
  alIniciar: (respuesta: RespuestaInicioSesion) => void
  /** Correo ya escrito, por ejemplo al llegar desde el registro (WCAG 3.3.7: no pedir dos veces el mismo dato). */
  correoInicial?: string
}) {
  const anunciar = useAnunciar()
  const [fraseVisible, setFraseVisible] = useState(false)
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<DatosInicioSesion>({
    resolver: zodResolver(esquemaInicioSesion),
    mode: 'onTouched',
    shouldFocusError: true,
    defaultValues: { correo: correoInicial, fraseContrasena: '' },
  })

  const inicioSesion = useMutation({
    mutationFn: (datos: DatosInicioSesion) => iniciarSesion(datos.correo, datos.fraseContrasena),
    onSuccess: (respuesta) => {
      anunciar(textos.sesionIniciada)
      alIniciar(respuesta)
    },
    onError: (error) => {
      if (esErrorApi(error) && error.esErrorDeValidacion) {
        error.errores.forEach(({ campo, mensaje }, indice) => {
          if (campo === 'correo' || campo === 'fraseContrasena') {
            setError(campo, { message: mensaje }, { shouldFocus: indice === 0 })
          }
        })
      }
    },
  })

  const errorGeneral =
    inicioSesion.error && !(esErrorApi(inicioSesion.error) && inicioSesion.error.esErrorDeValidacion)
      ? esErrorApi(inicioSesion.error)
        ? mensajeParaUsuario(inicioSesion.error)
        : 'No pudimos iniciar sesión. Inténtalo de nuevo.'
      : null

  return (
    <form noValidate onSubmit={handleSubmit((datos) => inicioSesion.mutate(datos))} className="flex flex-col gap-4">
      {errorGeneral ? (
        <p role="alert" className="rounded-control border border-peligro p-3 font-medium text-peligro">
          {errorGeneral}
        </p>
      ) : null}

      <CampoTexto
        etiqueta={textos.etiquetaCorreo}
        type="email"
        autoComplete="username"
        inputMode="email"
        error={errors.correo?.message}
        {...register('correo')}
      />

      <div className="flex flex-col gap-2">
        <CampoTexto
          etiqueta={textos.etiquetaFrase}
          pista={textos.pistaFrase}
          type={fraseVisible ? 'text' : 'password'}
          autoComplete="current-password"
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

      <Boton type="submit" ancho="completo" enviando={inicioSesion.isPending} textoEnviando={textos.botonEntrando}>
        {textos.botonEntrar}
      </Boton>
    </form>
  )
}
