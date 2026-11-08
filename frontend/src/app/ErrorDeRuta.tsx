import { isRouteErrorResponse, Link, useRouteError } from 'react-router'
import { EstadoError } from '@/components/Estados'

/** Límite de errores de cada ruta: dice qué pasó y deja reintentar (DECISIONES-FRONTEND sección 2.4). */
export function ErrorDeRuta() {
  const error = useRouteError()

  if (isRouteErrorResponse(error) && error.status === 404) {
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

  return (
    <section className="flex flex-col gap-4">
      <h1 className="text-2xl font-bold">Algo salió mal</h1>
      <EstadoError error={error} alReintentar={() => window.location.reload()} />
    </section>
  )
}
