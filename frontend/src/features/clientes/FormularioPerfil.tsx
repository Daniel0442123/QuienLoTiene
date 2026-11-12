import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { CampoTexto } from '@/components/CampoTexto'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { esErrorApi } from '@/services/errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from '@/services/mensajes-error'
import { useActualizarPerfil, type Perfil } from './api'
import { esquemaPerfil, type DatosPerfil, type EntradaPerfil } from './esquemas'
import { textosClientes as textos } from './textos'

/** Nombre y celular del cliente (HU-007, CU-03 pasos 14 a 16). El correo se muestra pero no se edita. */
export function FormularioPerfil({ perfil }: { perfil: Perfil }) {
  const anunciar = useAnunciar()
  const guardado = useActualizarPerfil()
  const [huboCambios, setHuboCambios] = useState(false)
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<EntradaPerfil, unknown, DatosPerfil>({
    resolver: zodResolver(esquemaPerfil),
    mode: 'onTouched',
    shouldFocusError: true,
    defaultValues: { nombre: perfil.nombre, telefono: perfil.telefono },
  })

  const error = guardado.error
  const errorDeCampo =
    esErrorApi(error) && error.errores.some(({ campo }) => campo === 'nombre' || campo === 'telefono')
  const errorGeneral =
    error && !errorDeCampo ? (esErrorApi(error) ? mensajeParaUsuario(error) : textos.errorGeneral) : null

  return (
    <form
      noValidate
      onSubmit={handleSubmit((datos) =>
        guardado.mutate(datos, {
          onSuccess: () => {
            setHuboCambios(true)
            anunciar(textos.perfilGuardado)
          },
          onError: (falla) => {
            if (!esErrorApi(falla)) return
            falla.errores.forEach(({ campo, mensaje }, indice) => {
              if (campo === 'nombre' || campo === 'telefono')
                setError(campo, { message: mensaje }, { shouldFocus: indice === 0 })
            })
          },
        }),
      )}
      onChange={() => setHuboCambios(false)}
      className="flex flex-col gap-4"
    >
      {errorGeneral ? (
        <div role="alert" className="rounded-control border border-peligro p-3 font-medium text-peligro">
          <p>{errorGeneral}</p>
          {esErrorApi(error) && debeMostrarIdCorrelacion(error) ? (
            <p className="mt-1 text-sm font-normal text-texto-suave">{textos.codigoSoporte(error.idCorrelacion)}</p>
          ) : null}
        </div>
      ) : null}
      <CampoTexto
        etiqueta={textos.etiquetaNombre}
        autoComplete="name"
        error={errors.nombre?.message}
        {...register('nombre')}
      />
      <CampoTexto
        etiqueta={textos.etiquetaCorreo}
        pista={textos.pistaCorreo}
        type="email"
        value={perfil.correo}
        readOnly
        className="bg-superficie"
      />
      <CampoTexto
        etiqueta={textos.etiquetaTelefono}
        pista={textos.pistaTelefono}
        type="tel"
        inputMode="tel"
        autoComplete="tel"
        error={errors.telefono?.message}
        {...register('telefono')}
      />
      <div className="flex flex-col gap-2">
        <Boton type="submit" className="self-start" enviando={guardado.isPending} textoEnviando={textos.botonGuardando}>
          {textos.botonGuardarPerfil}
        </Boton>
        {huboCambios && guardado.isSuccess ? <p className="font-medium text-exito">{textos.perfilGuardado}</p> : null}
      </div>
    </form>
  )
}
