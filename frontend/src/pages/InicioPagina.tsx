import { Link } from 'react-router'
import { useSesion } from '@/hooks/useSesion'

export function InicioPagina() {
  const sesion = useSesion()
  return (
    <section className="flex flex-col gap-4">
      <h1 className="text-2xl font-bold">¿Quién lo tiene?</h1>
      <p>Pregunta una vez y las farmacias y ferreterías cercanas te responden si tienen lo que buscas.</p>
      {sesion ? null : (
        <Link
          to="/entrar"
          className="inline-flex min-h-tactil items-center self-start rounded-control bg-primario px-4 font-semibold text-primario-texto"
        >
          Entrar
        </Link>
      )}
    </section>
  )
}
