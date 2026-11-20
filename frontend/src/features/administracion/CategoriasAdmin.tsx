import { useState } from 'react'
import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { useAnunciar } from '@/components/RegionAnuncios'
import { cn } from '@/components/ui/utilidades'
import { mensajeAdministracion, useCambiarCategoria, useCategoriasAdmin } from './api'
import { textosAdministracion as textos } from './textos'

/**
 * HU-038, CP-CAT-038-01: activar o desactivar categorías (mockup 07). Cada una es un interruptor (casilla con
 * `role="switch"`) con el estado también en texto: «Ferretería · inactiva». No pide confirmación porque se deshace
 * tocándolo de nuevo; el cambio se anuncia por la región aria-live.
 */
export function CategoriasAdmin() {
  const anunciar = useAnunciar()
  const categorias = useCategoriasAdmin()
  const cambio = useCambiarCategoria()
  const [guardando, setGuardando] = useState<string | null>(null)

  return (
    <section aria-labelledby="catalogo-categorias" className="flex flex-col gap-3">
      <h2 id="catalogo-categorias" className="text-xl font-semibold">
        {textos.tituloCategorias}
      </h2>
      {categorias.isPending ? (
        <EsqueletoLista filas={2} etiqueta={textos.cargandoCategorias} />
      ) : categorias.isError ? (
        <EstadoError error={categorias.error} alReintentar={() => void categorias.refetch()} />
      ) : (
        <ul className="flex flex-col gap-2">
          {categorias.data.map((categoria) => {
            const id = `categoria-${categoria.id}`
            return (
              <li key={categoria.id}>
                <label
                  htmlFor={id}
                  className="flex min-h-tactil cursor-pointer items-center justify-between gap-3 rounded-control border border-borde px-4 py-2 has-focus-visible:outline-2 has-focus-visible:outline-offset-2 has-focus-visible:outline-primario"
                >
                  <span>
                    {categoria.nombre} ·{' '}
                    <span className={categoria.activa ? 'font-semibold' : 'text-texto-suave'}>
                      {categoria.activa ? textos.categoriaActiva : textos.categoriaInactiva}
                    </span>
                  </span>
                  <input
                    id={id}
                    type="checkbox"
                    role="switch"
                    aria-checked={categoria.activa}
                    className="peer sr-only"
                    checked={categoria.activa}
                    disabled={guardando === categoria.id}
                    aria-busy={guardando === categoria.id || undefined}
                    onChange={() => {
                      setGuardando(categoria.id)
                      cambio.mutate(
                        { categoriaId: categoria.id, activa: !categoria.activa },
                        {
                          onSuccess: (actualizada) =>
                            anunciar(
                              actualizada.activa
                                ? textos.categoriaActivada(actualizada.nombre)
                                : textos.categoriaDesactivada(actualizada.nombre),
                            ),
                          onSettled: () => setGuardando(null),
                        },
                      )
                    }}
                  />
                  {/* Dibujo del interruptor: el estado ya está dicho en texto y en el rol switch */}
                  <span
                    aria-hidden="true"
                    className={cn(
                      'relative h-7 w-12 shrink-0 rounded-full border-2 transition-colors',
                      categoria.activa ? 'border-primario bg-primario' : 'border-borde bg-superficie',
                    )}
                  >
                    <span
                      className={cn(
                        'absolute top-0.5 size-5 rounded-full transition-all',
                        categoria.activa ? 'left-5 bg-fondo' : 'left-0.5 bg-texto-suave',
                      )}
                    />
                  </span>
                </label>
              </li>
            )
          })}
        </ul>
      )}
      {cambio.error ? (
        <p role="alert" className="font-medium text-peligro">
          {mensajeAdministracion(cambio.error)}
        </p>
      ) : null}
    </section>
  )
}
