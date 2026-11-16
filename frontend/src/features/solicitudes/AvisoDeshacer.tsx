import { X } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { Boton } from '@/components/ui/Boton'
import { textosSolicitudes as textos } from './textos'

/**
 * Aviso de «No lo tengo» con «Deshacer» y botón para cerrarlo (DECISIONES-FRONTEND §8, regla 7). La tarjeta
 * desaparece, así que el foco pasa a «Deshacer» para no perderse. El texto lo anuncia la región aria-live de la página.
 */
export function AvisoDeshacer({
  producto,
  alDeshacer,
  alCerrar,
}: {
  producto: string
  alDeshacer: () => void
  alCerrar: () => void
}) {
  const botonDeshacer = useRef<HTMLButtonElement>(null)
  useEffect(() => botonDeshacer.current?.focus(), [producto])

  return (
    <div className="sticky bottom-4 flex items-center gap-2 rounded-control bg-texto p-2 pl-4 text-fondo shadow-lg">
      <p className="flex-1 break-words">{textos.descartaste(producto)}</p>
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
