import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { useDomicilio } from '@/features/tiendas/api'
import { EncabezadoSeccionTienda } from '@/features/tiendas/EncabezadoSeccionTienda'
import { FormularioDomicilio } from '@/features/tiendas/FormularioDomicilio'
import { SimuladorDomicilio } from '@/features/tiendas/SimuladorDomicilio'
import { textosTiendas as textos } from '@/features/tiendas/textos'

/** /tienda/domicilio · HU-010 (CU-17). */
export function DomicilioTiendaPagina() {
  const domicilio = useDomicilio()
  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <EncabezadoSeccionTienda titulo={textos.seccionDomicilio} descripcion={textos.descripcionDomicilio} />
      {domicilio.isPending ? (
        <EsqueletoLista filas={2} etiqueta={textos.cargandoTienda} />
      ) : domicilio.isError ? (
        <EstadoError error={domicilio.error} alReintentar={() => void domicilio.refetch()} />
      ) : (
        <>
          <FormularioDomicilio domicilio={domicilio.data} />
          <SimuladorDomicilio domicilioGuardado={domicilio.data} />
        </>
      )}
    </section>
  )
}
