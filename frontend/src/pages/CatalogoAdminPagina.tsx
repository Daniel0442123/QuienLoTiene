import { useEffect } from 'react'
import { CategoriasAdmin } from '@/features/administracion/CategoriasAdmin'
import { NavegacionAdmin } from '@/features/administracion/NavegacionAdmin'
import { RestringidosAdmin } from '@/features/administracion/RestringidosAdmin'
import { textosAdministracion as textos } from '@/features/administracion/textos'

/** /admin/catalogo · HU-038 (mockup 07): categorías activas y productos restringidos. */
export function CatalogoAdminPagina() {
  useEffect(() => {
    document.title = `${textos.tituloCatalogo} · ¿Quién lo tiene?`
  }, [])

  return (
    <section className="mx-auto flex max-w-2xl flex-col gap-8">
      <NavegacionAdmin />
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold">{textos.tituloCatalogo}</h1>
        <p className="text-texto-suave">{textos.introCatalogo}</p>
      </div>
      <CategoriasAdmin />
      <RestringidosAdmin />
    </section>
  )
}
