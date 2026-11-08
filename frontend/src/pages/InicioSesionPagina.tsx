import { useLocation, useNavigate } from 'react-router'
import { FormularioInicioSesion } from '@/features/identidad/FormularioInicioSesion'
import { textosIdentidad } from '@/features/identidad/textos'

export function InicioSesionPagina() {
  const navegar = useNavigate()
  const { state } = useLocation()
  const volverA = (state as { volverA?: string } | null)?.volverA ?? '/'

  return (
    <section className="mx-auto flex max-w-md flex-col gap-6">
      <h1 className="text-2xl font-bold">{textosIdentidad.tituloInicioSesion}</h1>
      <FormularioInicioSesion
        alIniciar={(respuesta) => {
          // TODO HU-004: si respuesta.segundoFactorRequerido, ir a la pantalla del código TOTP
          if (!respuesta.segundoFactorRequerido) navegar(volverA, { replace: true })
        }}
      />
    </section>
  )
}
