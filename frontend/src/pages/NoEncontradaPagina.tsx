import { Link } from 'react-router'

export function NoEncontradaPagina() {
  return (
    <section className="flex flex-col gap-4">
      <h1 className="text-2xl font-bold">No encontramos esta página</h1>
      <p>
        Puede que el enlace esté mal escrito.{' '}
        <Link to="/" className="text-primario underline">
          Volver al inicio
        </Link>
      </p>
    </section>
  )
}
