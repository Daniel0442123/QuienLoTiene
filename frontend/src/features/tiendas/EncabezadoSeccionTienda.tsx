import { ChevronLeft } from 'lucide-react'
import { Link } from 'react-router'
import { textosTiendas as textos } from './textos'

/** Encabezado de cada sección del panel: volver al panel (siempre en el mismo lugar, WCAG 3.2.6), título y para qué sirve. */
export function EncabezadoSeccionTienda({ titulo, descripcion }: { titulo: string; descripcion: string }) {
  return (
    <div className="flex flex-col gap-2">
      <Link
        to="/tienda"
        className="inline-flex min-h-tactil items-center gap-1 self-start text-primario underline underline-offset-4 hover:no-underline"
      >
        <ChevronLeft aria-hidden="true" className="size-5" />
        {textos.volverAlPanel}
      </Link>
      <h1 className="text-2xl font-bold">{titulo}</h1>
      <p className="text-texto-suave">{descripcion}</p>
    </div>
  )
}
