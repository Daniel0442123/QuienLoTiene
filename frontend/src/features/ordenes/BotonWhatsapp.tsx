import { MessageCircle } from 'lucide-react'
import { useContactoWhatsapp } from './api'
import { textosOrdenes as textos } from './textos'

/**
 * HU-032: escribirle a la otra parte por WhatsApp. Solo con la orden activa (CP-MEN-032-01); antes de elegir no hay
 * orden y no se muestra (CP-MEN-032-02). Es un enlace: abre WhatsApp, no hace nada dentro de la app.
 */
export function BotonWhatsapp({ ordenId, esActiva }: { ordenId: string; esActiva: boolean }) {
  const contacto = useContactoWhatsapp(ordenId, esActiva)
  if (!esActiva) return null
  if (contacto.isPending) return <p role="status">{textos.cargandoWhatsapp}</p>
  if (contacto.isError) return <p className="text-texto-suave">{textos.errorWhatsapp}</p>

  return (
    <a
      href={contacto.data.enlace}
      target="_blank"
      rel="noopener noreferrer"
      className="inline-flex min-h-tactil items-center justify-center gap-2 rounded-control border border-borde bg-fondo px-4 font-semibold text-texto hover:bg-superficie"
    >
      <MessageCircle aria-hidden="true" className="size-5" />
      {textos.botonWhatsapp(contacto.data.nombre)}
      <span className="sr-only"> {textos.seAbreEnWhatsapp}</span>
    </a>
  )
}
