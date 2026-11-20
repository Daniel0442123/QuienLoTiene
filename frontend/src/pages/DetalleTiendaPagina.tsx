import { ChevronLeft, Navigation, Phone } from 'lucide-react'
import { lazy, Suspense, useEffect, useRef, type ReactNode } from 'react'
import { useNavigate, useParams } from 'react-router'
import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import { cn } from '@/components/ui/utilidades'
import { enlaceComoLlegar } from '@/features/ordenes/presentacion'
import { textosOrdenes } from '@/features/ordenes/textos'
import { useReputacionTienda } from '@/features/reputacion/api'
import { Estrellas } from '@/features/reputacion/Estrellas'
import { textosReputacion } from '@/features/reputacion/textos'
import { useTiendaPublica } from '@/features/tiendas/api'
import { horarioPorDia, mediosDePago } from '@/features/tiendas/presentacion'
import { textosTiendas as textos } from '@/features/tiendas/textos'
import { useCategorias } from '@/hooks/useCategorias'
import { esErrorApi } from '@/services/errores'
import { formatearDistancia, formatearFecha, formatearPesos } from '@/services/formato'

const MapaUbicacion = lazy(() => import('@/components/MapaUbicacion'))

const formatoPromedio = new Intl.NumberFormat('es-CO', { minimumFractionDigits: 1, maximumFractionDigits: 1 })

function Seccion({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <section className="flex flex-col gap-1 border-b border-borde pb-4">
      <h2 className="font-semibold">{titulo}</h2>
      {children}
    </section>
  )
}

/** HU-014 y HU-034: promedio y las 10 reseñas más recientes, sin el nombre del cliente. Va aparte de los datos. */
function ResenasDeLaTienda({ tiendaId }: { tiendaId: string }) {
  const reputacion = useReputacionTienda(tiendaId)
  return (
    <section aria-labelledby="tienda-resenas" className="flex flex-col gap-3">
      <h2 id="tienda-resenas" className="text-lg font-semibold">
        {textos.tituloResenas}
      </h2>
      {reputacion.isPending ? (
        <EsqueletoLista filas={2} etiqueta={textos.cargandoResenas} />
      ) : reputacion.isError ? (
        <EstadoError error={reputacion.error} alReintentar={() => void reputacion.refetch()} />
      ) : reputacion.data.totalResenas === 0 ? (
        <EstadoVacio titulo={textosReputacion.sinResenas}>{textos.sinResenasAyuda}</EstadoVacio>
      ) : (
        <>
          <p className="text-texto-suave">{textos.ultimasResenas(reputacion.data.recientes.length)}</p>
          <ul className="flex flex-col gap-3">
            {reputacion.data.recientes.map((resena, indice) => (
              <li
                key={`${resena.fecha}-${indice}`}
                className="flex flex-col gap-1 rounded-control border border-borde p-3"
              >
                <p className="flex flex-wrap items-center gap-2">
                  <Estrellas cantidad={resena.estrellas} tamano="pequeno" />
                  <span className="text-sm text-texto-suave">{formatearFecha(resena.fecha)}</span>
                </p>
                <p className={cn('break-words', !resena.comentario && 'text-texto-suave')}>
                  {resena.comentario ? `«${resena.comentario}»` : textos.resenaSinComentario}
                </p>
              </li>
            ))}
          </ul>
        </>
      )}
    </section>
  )
}

/**
 * /tiendas/:tiendaId · HU-014 (mockup 01): el detalle público de una tienda APROBADA para el cliente. Nunca muestra
 * el RUT ni los documentos (la API no los manda, CP-TIE-014-02). Los datos y las reseñas se piden en paralelo, por
 * rutas distintas (decisión 4 del contrato): si las reseñas fallan, los datos se ven igual. La foto del mockup no
 * está en la API todavía.
 */
export function DetalleTiendaPagina() {
  const { tiendaId = '' } = useParams()
  const navegar = useNavigate()
  const tienda = useTiendaPublica(tiendaId)
  const reputacion = useReputacionTienda(tiendaId)
  const categorias = useCategorias()
  const titulo = useRef<HTMLHeadingElement>(null)
  const nombre = tienda.data?.nombre

  useEffect(() => {
    if (!nombre) return
    document.title = `${nombre} · ¿Quién lo tiene?`
    titulo.current?.focus()
  }, [nombre])

  const volver = (
    <button
      type="button"
      onClick={() => void navegar(-1)}
      className="inline-flex min-h-tactil items-center gap-1 self-start text-primario underline underline-offset-4 hover:no-underline"
    >
      <ChevronLeft aria-hidden="true" className="size-5" />
      {textos.volver}
    </button>
  )

  if (tienda.isPending) {
    return (
      <section className="mx-auto flex max-w-xl flex-col gap-6">
        {volver}
        <EsqueletoLista filas={4} etiqueta={textos.cargandoTiendaPublica} />
      </section>
    )
  }
  if (tienda.isError) {
    const noDisponible = esErrorApi(tienda.error) && tienda.error.codigo === 'TIE-006'
    return (
      <section className="mx-auto flex max-w-xl flex-col gap-6">
        {volver}
        {noDisponible ? (
          <EstadoVacio titulo={textos.tiendaNoDisponible}>{textos.tiendaNoDisponibleAyuda}</EstadoVacio>
        ) : (
          <EstadoError error={tienda.error} alReintentar={() => void tienda.refetch()} />
        )}
      </section>
    )
  }

  const datos = tienda.data
  const categoria = categorias.data?.find((c) => c.id === datos.categoriaId)?.nombre
  const medios = mediosDePago(datos)
  const { haceDomicilio, radioCoberturaEnMetros, tarifaFijaEnPesos } = datos.domicilio
  const promedio = reputacion.data

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <div className="flex flex-col gap-2">
        {volver}
        <h1 ref={titulo} tabIndex={-1} className="text-2xl font-bold break-words">
          {datos.nombre}
        </h1>
        {categoria ? <p className="text-texto-suave">{categoria}</p> : null}
        <div className="flex flex-wrap items-center gap-2">
          {promedio ? (
            <p className="inline-flex items-center gap-2 rounded-full border border-borde px-3 py-1 font-semibold">
              {promedio.totalResenas > 0 ? (
                <>
                  <Estrellas cantidad={Math.round(promedio.promedio)} tamano="pequeno" />
                  {textosReputacion.promedio(formatoPromedio.format(promedio.promedio), promedio.totalResenas)}
                </>
              ) : (
                textosReputacion.sinResenas
              )}
            </p>
          ) : null}
          <p
            className={cn(
              'rounded-full border px-3 py-1 font-semibold',
              datos.estaAbierta ? 'border-exito text-exito' : 'border-borde text-texto-suave',
            )}
          >
            {datos.estaAbierta ? textos.abiertaAhora : textos.cerradaAhora}
          </p>
        </div>
      </div>

      <div className="flex flex-col gap-4">
        <Seccion titulo={textos.tituloHorario}>
          {datos.horario.length === 0 ? (
            <p className="text-texto-suave">{textos.sinHorario}</p>
          ) : (
            <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1">
              {horarioPorDia(datos.horario).map(({ dia, texto }) => (
                <div key={dia} className="contents">
                  <dt className="capitalize">{textos.dias[dia]}</dt>
                  <dd className={texto === textos.cerradoEseDia ? 'text-texto-suave' : undefined}>{texto}</dd>
                </div>
              ))}
            </dl>
          )}
        </Seccion>

        <Seccion titulo={textos.tituloTelefono}>
          <a
            href={`tel:${datos.telefono}`}
            className="inline-flex min-h-tactil items-center gap-2 self-start text-primario underline underline-offset-4 hover:no-underline"
          >
            <Phone aria-hidden="true" className="size-5" />
            {datos.telefono}
          </a>
        </Seccion>

        <Seccion titulo={textos.tituloUbicacion}>
          <Suspense fallback={<p role="status">{textos.cargandoMapa}</p>}>
            <MapaUbicacion valor={{ latitud: datos.latitud, longitud: datos.longitud }} lugar={datos.nombre} />
          </Suspense>
          <a
            href={enlaceComoLlegar(datos.latitud, datos.longitud)}
            target="_blank"
            rel="noopener noreferrer"
            className="mt-2 inline-flex min-h-tactil items-center justify-center gap-2 rounded-control border border-borde bg-fondo px-4 font-semibold text-texto hover:bg-superficie"
          >
            <Navigation aria-hidden="true" className="size-5" />
            {textosOrdenes.botonComoLlegar}
            <span className="sr-only"> {textosOrdenes.seAbreMapas}</span>
          </a>
        </Seccion>

        <Seccion titulo={textos.tituloDomicilio}>
          <p>
            {haceDomicilio && radioCoberturaEnMetros !== null && tarifaFijaEnPesos !== null
              ? textos.domicilioHasta(formatearDistancia(radioCoberturaEnMetros), formatearPesos(tarifaFijaEnPesos))
              : textos.soloRecoger}
          </p>
        </Seccion>

        <Seccion titulo={textos.tituloMediosPago}>
          <p className={medios.length === 0 ? 'text-texto-suave' : undefined}>
            {medios.length > 0 ? medios.map((medio) => textos.mediosPago[medio]).join(', ') : textos.sinMediosPago}
          </p>
        </Seccion>
      </div>

      <ResenasDeLaTienda tiendaId={tiendaId} />
    </section>
  )
}
