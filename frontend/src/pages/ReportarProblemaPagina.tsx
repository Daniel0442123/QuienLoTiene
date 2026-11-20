import { ChevronLeft } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { useAnunciar } from '@/components/RegionAnuncios'
import { useOrden } from '@/features/ordenes/api'
import { numeroCorto, type VistaOrden } from '@/features/ordenes/presentacion'
import { FormularioReportar } from '@/features/reputacion/FormularioReportar'
import { textosReputacion as textos } from '@/features/reputacion/textos'

/**
 * /pedidos/:ordenId/reportar y /tienda/pedidos/:ordenId/reportar · HU-035 (mockup 03): el cliente o la tienda reporta
 * un problema de su pedido. Queda ABIERTO para el administrador (HU-037). Al enviar vuelve al pedido y lo anuncia.
 */
export function ReportarProblemaPagina({ vista }: { vista: VistaOrden }) {
  const { ordenId = '' } = useParams()
  const navegar = useNavigate()
  const anunciar = useAnunciar()
  const orden = useOrden(ordenId)
  const titulo = useRef<HTMLHeadingElement>(null)
  const rutaPedido = vista === 'cliente' ? `/pedidos/${ordenId}` : `/tienda/pedidos/${ordenId}`

  useEffect(() => {
    document.title = `${textos.tituloReportar} · ¿Quién lo tiene?`
    titulo.current?.focus()
  }, [])

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <div className="flex flex-col gap-1">
        <Link
          to={rutaPedido}
          className="inline-flex min-h-tactil items-center gap-1 self-start text-primario underline underline-offset-4 hover:no-underline"
        >
          <ChevronLeft aria-hidden="true" className="size-5" />
          {textos.volverAlPedido}
        </Link>
        <h1 ref={titulo} tabIndex={-1} className="text-2xl font-bold">
          {textos.tituloReportar}
        </h1>
        {orden.data ? (
          <p className="text-texto-suave">
            {vista === 'cliente'
              ? textos.pedidoA(orden.data.tienda.nombre, numeroCorto(orden.data.id))
              : textos.pedidoDe(numeroCorto(orden.data.id))}
          </p>
        ) : null}
      </div>
      {orden.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargandoPedido} />
      ) : orden.isError ? (
        <EstadoError error={orden.error} alReintentar={() => void orden.refetch()} />
      ) : (
        <FormularioReportar
          ordenId={ordenId}
          alEnviar={() => {
            anunciar(textos.reporteEnviado)
            void navegar(rutaPedido)
          }}
        />
      )}
    </section>
  )
}

/** Rutas sin propiedades (lazy): una para el cliente y otra para la tienda. */
export function ReportarProblemaClientePagina() {
  return <ReportarProblemaPagina vista="cliente" />
}

export function ReportarProblemaTiendaPagina() {
  return <ReportarProblemaPagina vista="tienda" />
}
