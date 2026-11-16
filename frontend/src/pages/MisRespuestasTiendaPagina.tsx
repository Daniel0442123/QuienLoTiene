import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import { useMisRespuestas } from '@/features/solicitudes/api'
import { PestanasSolicitudesTienda } from '@/features/solicitudes/PestanasSolicitudesTienda'
import { TarjetaMiRespuesta } from '@/features/solicitudes/TarjetaMiRespuesta'
import { textosSolicitudes as textos } from '@/features/solicitudes/textos'
import { EncabezadoSeccionTienda } from '@/features/tiendas/EncabezadoSeccionTienda'

/** /tienda/solicitudes/mis-respuestas · HU-019 y HU-021: lo que respondió la tienda en 24 h y si la eligieron. */
export function MisRespuestasTiendaPagina() {
  const respuestas = useMisRespuestas()

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <EncabezadoSeccionTienda
        titulo={textos.tituloSolicitudesTienda}
        descripcion={textos.descripcionSolicitudesTienda}
      />
      <PestanasSolicitudesTienda />

      {respuestas.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargandoSolicitudes} />
      ) : respuestas.isError ? (
        <EstadoError error={respuestas.error} alReintentar={() => void respuestas.refetch()} />
      ) : respuestas.data.length === 0 ? (
        <EstadoVacio titulo={textos.sinMisRespuestas}>{textos.sinMisRespuestasAyuda}</EstadoVacio>
      ) : (
        <ul className="flex flex-col gap-3">
          {respuestas.data.map((respuesta) => (
            <li key={respuesta.id}>
              <TarjetaMiRespuesta respuesta={respuesta} />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
