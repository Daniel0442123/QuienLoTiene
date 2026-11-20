import { TriangleAlert } from 'lucide-react'
import { textosSolicitudes as textos } from './textos'

/**
 * HU-023, CP-SOL-023-01: «Este medicamento necesita fórmula médica» (SC-011 punto 7) y que no hay domicilio. El
 * título lo manda el backend; si no llega, se usa el mismo texto aprobado. Va con ícono y texto, no solo color.
 */
export function AvisoRestringido({ id, aviso }: { id?: string; aviso?: string | null }) {
  return (
    <div id={id} className="flex items-start gap-3 rounded-control border-2 border-advertencia p-3">
      <TriangleAlert aria-hidden="true" className="mt-0.5 size-5 shrink-0 text-advertencia" />
      <div className="flex flex-col gap-1">
        <p className="font-semibold">{aviso ?? textos.avisoRestringidoTitulo}</p>
        <p>{textos.avisoRestringidoDetalle}</p>
      </div>
    </div>
  )
}
