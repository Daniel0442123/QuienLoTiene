import { politicaDatos } from '@/services/politicaDatos'

/**
 * Texto de la política de datos (Ley 1581, HU-005) para leer antes de marcar la autorización.
 * Lo usan el registro de cliente (CU-01) y el de tienda (CU-15). Se despliega con <details>: nativo y accesible.
 */
export function PoliticaDatosDesplegable() {
  return (
    <details className="rounded-control border border-borde-suave bg-superficie">
      <summary className="flex min-h-tactil cursor-pointer items-center px-3 font-medium text-primario underline underline-offset-4">
        {politicaDatos.resumen}
      </summary>
      <div className="flex flex-col gap-3 px-3 pb-3">
        <h2 className="font-semibold">{politicaDatos.titulo}</h2>
        {politicaDatos.secciones.map((seccion) => (
          <section key={seccion.titulo}>
            <h3 className="font-medium">{seccion.titulo}</h3>
            <p className="text-texto-suave">{seccion.texto}</p>
          </section>
        ))}
      </div>
    </details>
  )
}
