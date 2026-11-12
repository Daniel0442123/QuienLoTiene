import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import { ESTADOS_TIENDA, useTiendasPorEstado, type EstadoTienda } from '@/features/administracion/api'
import { TarjetaTiendaRevision } from '@/features/administracion/TarjetaTiendaRevision'
import { textosAdministracion as textos } from '@/features/administracion/textos'
import { useCategorias } from '@/hooks/useCategorias'

function esEstado(valor: string | null): valor is EstadoTienda {
  return ESTADOS_TIENDA.includes(valor as EstadoTienda)
}

/**
 * /admin/tiendas · HU-036 (CU-26). El estado elegido va en la URL (?estado=APROBADA) para que «Atrás» y recargar
 * mantengan la lista. Por defecto, las pendientes.
 */
export function RevisarTiendasPagina() {
  const [parametros, setParametros] = useSearchParams()
  const estadoPedido = parametros.get('estado')
  const estado: EstadoTienda = esEstado(estadoPedido) ? estadoPedido : 'PENDIENTE'
  const tiendas = useTiendasPorEstado(estado)
  const categorias = useCategorias()
  const [ultimoAviso, setUltimoAviso] = useState<string | null>(null)

  const nombreCategoria = (categoriaId: string) =>
    categorias.data?.find((categoria) => categoria.id === categoriaId)?.nombre ?? textos.categoriaDesconocida

  return (
    <section className="mx-auto flex max-w-2xl flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold">{textos.tituloRevision}</h1>
        <p className="text-texto-suave">{textos.introRevision}</p>
      </div>

      <fieldset className="flex flex-col gap-2">
        <legend className="mb-1 font-semibold">{textos.filtroEstado}</legend>
        <div className="flex flex-wrap gap-2">
          {ESTADOS_TIENDA.map((opcion) => (
            <label
              key={opcion}
              className="flex min-h-tactil cursor-pointer items-center gap-2 rounded-control border border-borde px-3 has-checked:border-primario has-checked:bg-superficie has-checked:font-semibold"
            >
              <input
                type="radio"
                name="estado-tienda"
                className="size-5 accent-primario"
                checked={estado === opcion}
                onChange={() => {
                  setUltimoAviso(null)
                  setParametros(opcion === 'PENDIENTE' ? {} : { estado: opcion })
                }}
              />
              {textos.estados[opcion]}
            </label>
          ))}
        </div>
      </fieldset>

      {ultimoAviso ? (
        // Sin role="status": la tarjeta ya lo anunció por la región de avisos; aquí queda visible
        <p className="rounded-control border border-borde p-3 font-medium">{ultimoAviso}</p>
      ) : null}

      {tiendas.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargando} />
      ) : tiendas.isError ? (
        <EstadoError error={tiendas.error} alReintentar={() => void tiendas.refetch()} />
      ) : tiendas.data.length === 0 ? (
        <EstadoVacio titulo={textos.vacio[estado]}>{estado === 'PENDIENTE' ? textos.vacioAyuda : null}</EstadoVacio>
      ) : (
        <ul className="flex flex-col gap-4">
          {tiendas.data.map((tienda) => (
            <li key={tienda.id}>
              <TarjetaTiendaRevision
                tienda={tienda}
                nombreCategoria={nombreCategoria(tienda.categoriaId)}
                alRevisar={setUltimoAviso}
              />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
