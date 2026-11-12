import { Link, useLocation, useNavigate } from 'react-router'
import { FormularioInicioSesion } from '@/features/identidad/FormularioInicioSesion'
import { textosIdentidad } from '@/features/identidad/textos'

const destinoPorRol: Partial<Record<string, string>> = { TIENDA: '/tienda', CLIENTE: '/solicitudes/nueva' }

interface EstadoInicioSesion {
  volverA?: string
  /** Llega desde el registro (CU-01 paso 14): se avisa y se deja el correo escrito. */
  correoRegistrado?: string
}

export function InicioSesionPagina() {
  const navegar = useNavigate()
  const { state } = useLocation()
  const { volverA, correoRegistrado } = (state as EstadoInicioSesion | null) ?? {}

  return (
    <section className="mx-auto flex max-w-md flex-col gap-6">
      <h1 className="text-2xl font-bold">{textosIdentidad.tituloInicioSesion}</h1>
      {correoRegistrado ? (
        <p className="rounded-control border border-exito p-3 font-medium text-exito">{textosIdentidad.cuentaCreada}</p>
      ) : null}
      <FormularioInicioSesion
        correoInicial={correoRegistrado}
        alIniciar={(respuesta) => {
          // HU-004: el administrador escribe el código de su app autenticadora antes de tener sesión
          if (respuesta.segundoFactorRequerido) navegar('/entrar/codigo', { state: { volverA } })
          // Sin destino pedido, cada rol llega a su pantalla principal: la tienda a su panel y el cliente directo a
          // preguntar (RNF-027: publicar en ≤ 60 s)
          else navegar(volverA ?? destinoPorRol[respuesta.rol] ?? '/', { replace: true })
        }}
      />
      <p>
        {textosIdentidad.sinCuenta}{' '}
        <Link to="/crear-cuenta" className="text-primario underline underline-offset-4 hover:no-underline">
          {textosIdentidad.enlaceCrearCuenta}
        </Link>
      </p>
    </section>
  )
}
