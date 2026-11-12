import { Link, Navigate, useNavigate } from 'react-router'
import { FormularioRegistroCliente } from '@/features/identidad/FormularioRegistroCliente'
import { textosIdentidad as textos } from '@/features/identidad/textos'
import { useSesion } from '@/hooks/useSesion'

/** /crear-cuenta · HU-001 y HU-005 (CU-01). Al terminar lleva a entrar con el correo ya escrito (WCAG 3.3.7). */
export function RegistroClientePagina() {
  const sesion = useSesion()
  const navegar = useNavigate()

  if (sesion) return <Navigate to="/" replace />

  return (
    <section className="mx-auto flex max-w-md flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold">{textos.tituloRegistro}</h1>
        <p className="text-texto-suave">{textos.introRegistro}</p>
      </div>
      <FormularioRegistroCliente
        alRegistrar={(correo) => navegar('/entrar', { replace: true, state: { correoRegistrado: correo } })}
      />
      <p>
        {textos.yaTienesCuenta}{' '}
        <Link to="/entrar" className="text-primario underline underline-offset-4 hover:no-underline">
          {textos.enlaceEntrar}
        </Link>
      </p>
    </section>
  )
}
