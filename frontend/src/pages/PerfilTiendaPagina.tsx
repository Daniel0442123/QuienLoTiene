import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { useMiTienda } from '@/features/tiendas/api'
import { EncabezadoSeccionTienda } from '@/features/tiendas/EncabezadoSeccionTienda'
import { FormularioPerfilTienda } from '@/features/tiendas/FormularioPerfilTienda'
import { textosTiendas as textos } from '@/features/tiendas/textos'

/** /tienda/perfil · HU-009 (CU-16). */
export function PerfilTiendaPagina() {
  const miTienda = useMiTienda()
  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <EncabezadoSeccionTienda titulo={textos.seccionPerfil} descripcion={textos.descripcionPerfil} />
      {miTienda.isPending ? (
        <EsqueletoLista filas={4} etiqueta={textos.cargandoTienda} />
      ) : miTienda.isError ? (
        <EstadoError error={miTienda.error} alReintentar={() => void miTienda.refetch()} />
      ) : (
        <FormularioPerfilTienda tienda={miTienda.data} />
      )}
    </section>
  )
}
