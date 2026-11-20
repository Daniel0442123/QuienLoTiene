import { ChevronLeft } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { useAnunciar } from '@/components/RegionAnuncios'
import { useOrden } from '@/features/ordenes/api'
import { useResenaDeOrden } from '@/features/reputacion/api'
import { FormularioCalificar } from '@/features/reputacion/FormularioCalificar'
import { ResenaDelPedido } from '@/features/reputacion/ResenaDelPedido'
import { textosReputacion as textos } from '@/features/reputacion/textos'

/**
 * /pedidos/:ordenId/calificar · HU-034 (mockup 03): calificar la tienda de un pedido RESUELTA, una sola vez. Si el
 * pedido no está resuelto o ya tiene calificación, se dice en vez de mostrar el formulario. Al publicar vuelve al
 * pedido, que ya muestra la calificación, y lo anuncia por la región aria-live.
 */
export function CalificarPedidoPagina() {
  const { ordenId = '' } = useParams()
  const navegar = useNavigate()
  const anunciar = useAnunciar()
  const orden = useOrden(ordenId)
  const resuelta = orden.data?.estado === 'RESUELTA'
  const resena = useResenaDeOrden(ordenId, resuelta)
  const titulo = useRef<HTMLHeadingElement>(null)

  useEffect(() => {
    document.title = `${textos.tituloDocumento} · ¿Quién lo tiene?`
  }, [])
  const listo = Boolean(orden.data)
  useEffect(() => {
    if (listo) titulo.current?.focus()
  }, [listo])

  const volver = (
    <Link
      to={`/pedidos/${ordenId}`}
      className="inline-flex min-h-tactil items-center gap-1 self-start text-primario underline underline-offset-4 hover:no-underline"
    >
      <ChevronLeft aria-hidden="true" className="size-5" />
      {textos.volverAlPedido}
    </Link>
  )

  if (orden.isPending || (resuelta && resena.isPending)) {
    return (
      <section className="mx-auto flex max-w-xl flex-col gap-6">
        {volver}
        <EsqueletoLista filas={3} etiqueta={textos.cargando} />
      </section>
    )
  }
  if (orden.isError) {
    return (
      <section className="mx-auto flex max-w-xl flex-col gap-6">
        {volver}
        <EstadoError error={orden.error} alReintentar={() => void orden.refetch()} />
      </section>
    )
  }

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <div className="flex flex-col gap-1">
        {volver}
        <h1 ref={titulo} tabIndex={-1} className="text-2xl font-bold break-words">
          {resena.data ? textos.yaCalificaste : textos.tituloCalificar(orden.data.tienda.nombre)}
        </h1>
        {!resena.data && resuelta ? <p className="text-texto-suave">{textos.ayudaCalificar}</p> : null}
      </div>
      {!resuelta ? (
        <p>{textos.noSePuedeCalificar}</p>
      ) : resena.data ? (
        <ResenaDelPedido orden={orden.data} vista="cliente" />
      ) : (
        <FormularioCalificar
          ordenId={ordenId}
          alPublicar={() => {
            anunciar(textos.publicada)
            void navegar(`/pedidos/${ordenId}`)
          }}
        />
      )}
    </section>
  )
}
