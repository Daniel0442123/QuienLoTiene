import { useEffect } from 'react'
import { useSearchParams } from 'react-router'
import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import {
  ESTADOS_REPORTE,
  useReportes,
  type EstadoReporte,
  type ReporteParaAdministrador,
} from '@/features/administracion/api'
import { NavegacionAdmin } from '@/features/administracion/NavegacionAdmin'
import { TarjetaReporte, type TiendaDelReporte } from '@/features/administracion/TarjetaReporte'
import { textosAdministracion as textos } from '@/features/administracion/textos'
import { numeroCorto } from '@/features/ordenes/presentacion'

function esEstadoReporte(valor: string | null): valor is EstadoReporte {
  return ESTADOS_REPORTE.includes(valor as EstadoReporte)
}

/**
 * /admin/reportes · HU-037 (mockup 07): los reportes abiertos, del más antiguo al más reciente, para resolverlos y,
 * si hace falta, suspender o reactivar la tienda. El filtro va en la URL (?estado=RESUELTO), como en «Revisar tiendas».
 * El reporte trae el nombre y el estado de su tienda (#74), así que no se piden las tiendas aparte. Si la tienda ya
 * no existe, llegan en null y se muestra su número.
 */
export function ReportesAdminPagina() {
  const [parametros, setParametros] = useSearchParams()
  const pedido = parametros.get('estado')
  const estado: EstadoReporte = esEstadoReporte(pedido) ? pedido : 'ABIERTO'
  const reportes = useReportes(estado)

  useEffect(() => {
    document.title = `${textos.tituloReportes} · ¿Quién lo tiene?`
  }, [])

  const tiendaDe = (reporte: ReporteParaAdministrador): TiendaDelReporte => ({
    nombre: reporte.nombreTienda ?? textos.tiendaSinNombre(numeroCorto(reporte.tiendaId)),
    estado: reporte.estadoTienda,
  })

  return (
    <section className="mx-auto flex max-w-2xl flex-col gap-6">
      <NavegacionAdmin />
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold">{textos.tituloReportes}</h1>
        <p className="text-texto-suave">{textos.introReportes}</p>
      </div>

      <fieldset className="flex flex-col gap-2">
        <legend className="mb-1 font-semibold">{textos.filtroReportes}</legend>
        <div className="flex flex-wrap gap-2">
          {ESTADOS_REPORTE.map((opcion) => (
            <label
              key={opcion}
              className="flex min-h-tactil cursor-pointer items-center gap-2 rounded-control border border-borde px-3 has-checked:border-primario has-checked:bg-superficie has-checked:font-semibold"
            >
              <input
                type="radio"
                name="estado-reporte"
                value={opcion}
                checked={estado === opcion}
                onChange={() => setParametros(opcion === 'ABIERTO' ? {} : { estado: opcion })}
                className="size-5 accent-primario"
              />
              {textos.estadosReporte[opcion]}
            </label>
          ))}
        </div>
      </fieldset>

      {reportes.isPending ? (
        <EsqueletoLista filas={2} etiqueta={textos.cargandoReportes} />
      ) : reportes.isError ? (
        <EstadoError error={reportes.error} alReintentar={() => void reportes.refetch()} />
      ) : reportes.data.length === 0 ? (
        <EstadoVacio titulo={textos.sinReportes[estado]}>{textos.sinReportesAyuda}</EstadoVacio>
      ) : (
        <ul className="flex flex-col gap-4">
          {reportes.data.map((reporte) => (
            <li key={reporte.id}>
              <TarjetaReporte reporte={reporte} tienda={tiendaDe(reporte)} />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
