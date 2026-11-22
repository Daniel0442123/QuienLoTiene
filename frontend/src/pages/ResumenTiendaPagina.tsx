import { useState } from 'react'
import { CampoSeleccion } from '@/components/CampoSeleccion'
import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { useIndicadores, type IndicadoresTienda, type PeriodoIndicadores } from '@/features/tiendas/api'
import { EncabezadoSeccionTienda } from '@/features/tiendas/EncabezadoSeccionTienda'
import { textosTiendas as textos } from '@/features/tiendas/textos'

const PERIODOS: PeriodoIndicadores[] = ['7', '30']

function Conteo({ valor, etiqueta }: { valor: number; etiqueta: string }) {
  return (
    // dt antes de dd como pide HTML; flex-col-reverse pone el número arriba como en el mockup
    <div className="flex flex-col-reverse items-center gap-1 rounded-control bg-superficie px-2 py-4">
      <dt className="text-texto-suave">{etiqueta}</dt>
      <dd className="text-3xl font-bold text-primario">{valor}</dd>
    </div>
  )
}

/** Barra con el porcentaje; sin denominador muestra «—» (SC-011 punto 6) y la barra vacía. */
function Porcentaje({
  etiqueta,
  parte,
  total,
  porcentaje,
}: {
  etiqueta: string
  parte: number
  total: number
  porcentaje: number | null
}) {
  const conDatos = porcentaje !== null
  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-baseline justify-between gap-2">
        <span>{etiqueta}</span>
        <span className="font-semibold">
          {conDatos ? (
            textos.proporcion(parte, total, porcentaje)
          ) : (
            <>
              <span aria-hidden="true">{textos.sinDatos}</span>
              <span className="sr-only">{textos.sinDatosLargo}</span>
            </>
          )}
        </span>
      </div>
      <div className="h-3 overflow-hidden rounded-full bg-borde-suave" aria-hidden="true">
        <div className="h-full rounded-full bg-primario" style={{ width: `${conDatos ? porcentaje : 0}%` }} />
      </div>
    </div>
  )
}

function Resumen({ indicadores }: { indicadores: IndicadoresTienda }) {
  return (
    <div className="flex flex-col gap-5">
      <dl className="grid grid-cols-3 gap-2">
        <Conteo valor={indicadores.recibidas} etiqueta={textos.recibidas} />
        <Conteo valor={indicadores.respondidas} etiqueta={textos.respondidas} />
        <Conteo valor={indicadores.ganadas} etiqueta={textos.ganadas} />
      </dl>
      <Porcentaje
        etiqueta={textos.respondiste}
        parte={indicadores.respondidas}
        total={indicadores.recibidas}
        porcentaje={indicadores.porcentajeRespondiste}
      />
      <Porcentaje
        etiqueta={textos.teEligieron}
        parte={indicadores.ganadas}
        total={indicadores.respuestasConProducto}
        porcentaje={indicadores.porcentajeTeEligieron}
      />
      <div className="flex flex-col gap-1 rounded-control bg-superficie p-4 text-texto-suave">
        <p>{textos.explicacionGanadas}</p>
        <p>{textos.explicacionTeEligieron}</p>
      </div>
    </div>
  )
}

/** /tienda/resumen · HU-015 (CU-23, mockup 06). */
export function ResumenTiendaPagina() {
  const [periodo, setPeriodo] = useState<PeriodoIndicadores>('7')
  const indicadores = useIndicadores(periodo)
  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <EncabezadoSeccionTienda titulo={textos.seccionResumen} descripcion={textos.descripcionResumen} />
      <div className="max-w-xs">
        <CampoSeleccion
          etiqueta={textos.etiquetaPeriodo}
          value={periodo}
          onChange={(evento) => setPeriodo(evento.target.value as PeriodoIndicadores)}
          opciones={PERIODOS.map((valor) => ({ valor, texto: textos.periodos[valor] }))}
        />
      </div>
      {indicadores.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargandoResumen} />
      ) : indicadores.isError ? (
        <EstadoError error={indicadores.error} alReintentar={() => void indicadores.refetch()} />
      ) : (
        <Resumen indicadores={indicadores.data} />
      )}
    </section>
  )
}
