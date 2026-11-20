import { useQueryClient } from '@tanstack/react-query'
import { Clock, TimerOff, XCircle } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { useAnunciar } from '@/components/RegionAnuncios'
import { useAhora } from '@/hooks/useAhora'
import { formatearHora, minutosHasta } from '@/services/formato'
import { clavesOrdenes, type Orden } from './api'
import type { VistaOrden } from './presentacion'
import { textosOrdenes as textos } from './textos'

/** Hora en que la orden pasó a un estado, según su historial. */
function horaDel(orden: Orden, estado: Orden['estado']): string | null {
  const cambio = orden.historial.findLast((c) => c.estado === estado)
  return cambio ? formatearHora(cambio.fecha) : null
}

/**
 * HU-029: quién canceló, a qué hora y por qué (mockup 09). El motivo lo escribió la otra parte y llega también en el
 * aviso en tiempo real. Si canceló el cliente, la tienda lee además que no lo prepare ni lo envíe.
 */
export function AvisoCancelado({ orden, vista }: { orden: Orden; vista: VistaOrden }) {
  if (orden.estado !== 'CANCELADO') return null
  const hora = horaDel(orden, 'CANCELADO') ?? ''
  const quien = orden.canceladaPor ?? (vista === 'cliente' ? 'TIENDA' : 'CLIENTE')
  const frase =
    quien === 'CLIENTE'
      ? textos.canceladoPor.CLIENTE[vista](hora)
      : vista === 'cliente'
        ? textos.canceladoPor.TIENDA.cliente(hora, orden.tienda.nombre)
        : textos.canceladoPor.TIENDA.tienda(hora)
  return (
    <div className="flex flex-col gap-1 rounded-control border-2 border-peligro p-4">
      <p className="flex items-start gap-2 font-semibold text-peligro">
        <XCircle aria-hidden="true" className="mt-0.5 size-5 shrink-0" />
        {frase}
      </p>
      {orden.motivoCancelacion ? (
        <p className="break-words">{textos.motivoCancelacion(orden.motivoCancelacion)}</p>
      ) : null}
      {vista === 'tienda' && quien === 'CLIENTE' ? <p>{textos.noLoPrepares}</p> : null}
    </div>
  )
}

/** HU-025, CP-ORD-025-01: la recogida no llegó a tiempo y la orden quedó VENCIDA (mockup C-15). */
export function AvisoReservaVencida({ orden, vista }: { orden: Orden; vista: VistaOrden }) {
  if (orden.estado !== 'VENCIDA') return null
  const hora = horaDel(orden, 'VENCIDA') ?? (orden.reservaVence ? formatearHora(orden.reservaVence) : '')
  return (
    <div className="flex flex-col gap-1 rounded-control border-2 border-advertencia p-4">
      <p className="flex items-center gap-2 font-semibold text-advertencia">
        <TimerOff aria-hidden="true" className="size-5 shrink-0" />
        {textos.tituloReservaVencida}
      </p>
      <p>{vista === 'cliente' ? textos.reservaVencidaCliente(hora) : textos.reservaVencidaTienda(hora)}</p>
    </div>
  )
}

const MINUTOS_AVISO = 5

/**
 * HU-025 (SC-019): la recogida lista se guarda 30 minutos desde que la tienda marcó «Listo para recoger». Muestra la
 * hora límite y los minutos que faltan (mockup 02), recalculados con el reloj cada 15 s. Para no llenar el lector de
 * pantalla, los minutos no son una región viva: solo se anuncia una vez cuando quedan 5 (WCAG 4.1.3). Al pasar la
 * hora se vuelve a pedir la orden para mostrar «Vencido» sin esperar al aviso en tiempo real.
 */
export function ReservaParaRecoger({ orden, vista }: { orden: Orden; vista: VistaOrden }) {
  const ahora = useAhora(15_000)
  const anunciar = useAnunciar()
  const consultas = useQueryClient()
  const avisado = useRef(false)
  const vigente = orden.estado === 'LISTO_PARA_RECOGER' && orden.reservaVence !== null
  const minutos = vigente && orden.reservaVence ? minutosHasta(orden.reservaVence, ahora) : null

  useEffect(() => {
    if (minutos === null) return
    if (vista === 'cliente' && minutos <= MINUTOS_AVISO && minutos > 0 && !avisado.current) {
      avisado.current = true
      anunciar(textos.avisoCincoMinutos)
    }
    if (minutos === 0) void consultas.invalidateQueries({ queryKey: clavesOrdenes.orden(orden.id) })
  }, [minutos, vista, anunciar, consultas, orden.id])

  if (!vigente || !orden.reservaVence || minutos === null) return null
  const hora = formatearHora(orden.reservaVence)
  return (
    <div className="flex flex-col items-center gap-1 rounded-control border-2 border-primario bg-superficie p-4 text-center">
      <p className="flex items-center gap-2">
        <Clock aria-hidden="true" className="size-5 shrink-0 text-primario" />
        {vista === 'cliente' ? textos.reservaHasta(hora) : textos.reservaHastaTienda(hora)}
      </p>
      <p className="text-4xl font-bold text-primario">
        <span aria-hidden="true">{textos.minutosRestantes(minutos)}</span>
        <span className="sr-only">{textos.quedanMinutos(minutos)}</span>
      </p>
      <p className="text-texto-suave">
        {vista === 'cliente' ? textos.reservaExplicaCliente : textos.reservaExplicaTienda}
      </p>
    </div>
  )
}
