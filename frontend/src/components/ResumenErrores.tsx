import { useId } from 'react'
import { enfocarCampo } from './enfocarCampo'

export interface ErrorDeResumen {
  /** id del campo con el error: el enlace lleva el foco ahí. */
  idCampo: string
  mensaje: string
}

/**
 * Resumen de errores arriba del formulario, con un enlace a cada campo (DECISIONES-FRONTEND sección 9, WCAG 3.3.1).
 * El foco lo lleva React Hook Form al primer campo con error; el resumen sirve para ver todo lo que falta de una vez.
 */
export function ResumenErrores({ titulo, errores }: { titulo: string; errores: ErrorDeResumen[] }) {
  const idTitulo = useId()
  if (errores.length === 0) return null

  return (
    <section aria-labelledby={idTitulo} className="rounded-control border-2 border-peligro p-4">
      <h2 id={idTitulo} className="font-semibold text-peligro">
        {titulo}
      </h2>
      <ul className="mt-2 flex list-disc flex-col gap-1 pl-5">
        {errores.map(({ idCampo, mensaje }) => (
          <li key={idCampo}>
            <a
              href={`#${idCampo}`}
              className="inline-flex min-h-tactil items-center text-primario underline underline-offset-4 hover:no-underline"
              onClick={(evento) => {
                // Solo mover el foco: cambiar el # de la URL no aporta y el enrutador no lo necesita
                evento.preventDefault()
                enfocarCampo(idCampo)
              }}
            >
              {mensaje}
            </a>
          </li>
        ))}
      </ul>
    </section>
  )
}
