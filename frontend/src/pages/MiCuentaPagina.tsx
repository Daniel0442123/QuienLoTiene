import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { usePerfil } from '@/features/clientes/api'
import { FormularioPerfil } from '@/features/clientes/FormularioPerfil'
import { ListaDirecciones } from '@/features/clientes/ListaDirecciones'
import { textosClientes as textos } from '@/features/clientes/textos'

/** /cuenta · HU-007 (CU-03): tus datos y tus direcciones. */
export function MiCuentaPagina() {
  const perfil = usePerfil()
  return (
    <div className="mx-auto flex max-w-xl flex-col gap-8">
      <h1 className="text-2xl font-bold">{textos.tituloMiCuenta}</h1>
      {perfil.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargando} />
      ) : perfil.isError ? (
        <EstadoError error={perfil.error} alReintentar={() => void perfil.refetch()} />
      ) : (
        <>
          <section aria-labelledby="titulo-perfil" className="flex flex-col gap-4">
            <h2 id="titulo-perfil" className="text-xl font-semibold">
              {textos.tituloPerfil}
            </h2>
            <FormularioPerfil perfil={perfil.data} />
          </section>
          <ListaDirecciones telefonoSugerido={perfil.data.telefono} />
        </>
      )}
    </div>
  )
}
