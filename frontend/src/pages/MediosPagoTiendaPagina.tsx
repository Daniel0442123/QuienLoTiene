import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { useMediosPago } from '@/features/tiendas/api'
import { EncabezadoSeccionTienda } from '@/features/tiendas/EncabezadoSeccionTienda'
import { FormularioMediosPago } from '@/features/tiendas/FormularioMediosPago'
import { textosTiendas as textos } from '@/features/tiendas/textos'

/** /tienda/medios-pago · HU-012 (CU-18). */
export function MediosPagoTiendaPagina() {
  const medios = useMediosPago()
  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <EncabezadoSeccionTienda titulo={textos.seccionMediosPago} descripcion={textos.descripcionMediosPago} />
      {medios.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargandoTienda} />
      ) : medios.isError ? (
        <EstadoError error={medios.error} alReintentar={() => void medios.refetch()} />
      ) : (
        <FormularioMediosPago mediosPago={medios.data.mediosPago} />
      )}
    </section>
  )
}
