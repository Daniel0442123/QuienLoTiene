import { X } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { Boton } from '@/components/ui/Boton'

const textos = { deshacer: 'Deshacer', cerrarAviso: 'Cerrar aviso' }

/**
 * Aviso con «Deshacer» y botón para cerrarlo (DECISIONES-FRONTEND §8, regla 7): «No lo tengo» (HU-018) y quitar un
 * producto restringido (HU-038). El elemento desaparece, así que el foco pasa a «Deshacer» para no perderse. El texto
 * lo anuncia la región aria-live de la página. Se usa con useDescartarConDeshacer.
 */
export function AvisoDeshacer({
  mensaje,
  alDeshacer,
  alCerrar,
}: {
  mensaje: string
  alDeshacer: () => void
  alCerrar: () => void
}) {
  const botonDeshacer = useRef<HTMLButtonElement>(null)
  useEffect(() => botonDeshacer.current?.focus(), [mensaje])

  return (
    <div className="sticky bottom-4 flex items-center gap-2 rounded-control bg-texto p-2 pl-4 text-fondo shadow-lg">
      <p className="flex-1 break-words">{mensaje}</p>
      <Boton
        ref={botonDeshacer}
        type="button"
        variante="secundario"
        className="border-fondo bg-texto text-fondo hover:bg-texto-suave"
        onClick={alDeshacer}
      >
        {textos.deshacer}
      </Boton>
      <Boton
        type="button"
        variante="secundario"
        className="border-transparent bg-texto text-fondo hover:bg-texto-suave"
        aria-label={textos.cerrarAviso}
        onClick={alCerrar}
      >
        <X aria-hidden="true" className="size-5" />
      </Boton>
    </div>
  )
}
