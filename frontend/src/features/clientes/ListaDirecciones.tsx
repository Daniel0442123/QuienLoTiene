import { MapPin, Pencil, Plus, Trash2 } from 'lucide-react'
import { useRef, useState } from 'react'
import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { useDirecciones } from '@/hooks/useDirecciones'
import { MAXIMO_DIRECCIONES, useBorrarDireccion, type Direccion } from './api'
import { FormularioDireccion } from './FormularioDireccion'
import { textosClientes as textos } from './textos'

type Edicion = { tipo: 'nueva' } | { tipo: 'editar'; id: string } | { tipo: 'borrar'; id: string } | null

function TarjetaDireccion({
  direccion,
  alEditar,
  alPedirBorrar,
}: {
  direccion: Direccion
  alEditar: () => void
  alPedirBorrar: () => void
}) {
  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-start gap-3">
        <MapPin aria-hidden="true" className="mt-1 size-5 shrink-0 text-primario" />
        <div className="flex min-w-0 flex-col">
          <h3 className="font-semibold break-words">{direccion.nombre}</h3>
          <p className="break-words">{direccion.direccionEscrita}</p>
          {direccion.indicaciones ? (
            <p className="text-sm break-words text-texto-suave">{textos.indicaciones(direccion.indicaciones)}</p>
          ) : null}
          <p className="text-sm text-texto-suave">{textos.telefonoDireccion(direccion.telefono)}</p>
        </div>
      </div>
      <div className="flex flex-wrap gap-2">
        <Boton type="button" variante="secundario" onClick={alEditar} aria-label={textos.editar(direccion.nombre)}>
          <Pencil aria-hidden="true" className="size-4" />
          {textos.botonEditar}
        </Boton>
        <Boton type="button" variante="secundario" onClick={alPedirBorrar} aria-label={textos.borrar(direccion.nombre)}>
          <Trash2 aria-hidden="true" className="size-4" />
          {textos.botonBorrar}
        </Boton>
      </div>
    </div>
  )
}

/**
 * Mis direcciones (HU-007, CU-03 pasos 1 a 13): ver, agregar, editar y borrar. Máximo 10 (CLI-002): al llegar al
 * límite, «Agregar dirección» queda deshabilitado con la razón visible. Borrar pide confirmar en la misma tarjeta.
 */
export function ListaDirecciones({ telefonoSugerido }: { telefonoSugerido: string }) {
  const anunciar = useAnunciar()
  const direcciones = useDirecciones()
  const borrado = useBorrarDireccion()
  const [edicion, setEdicion] = useState<Edicion>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const titulo = useRef<HTMLHeadingElement>(null)

  const terminar = (mensaje: string) => {
    setEdicion(null)
    setAviso(mensaje)
    anunciar(mensaje)
    // El formulario desaparece: el foco vuelve al título de la sección para no perderse
    requestAnimationFrame(() => titulo.current?.focus())
  }

  const lleno = (direcciones.data?.length ?? 0) >= MAXIMO_DIRECCIONES

  return (
    <section aria-labelledby="titulo-direcciones" className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <h2 id="titulo-direcciones" ref={titulo} tabIndex={-1} className="text-xl font-semibold">
          {textos.tituloDirecciones}
        </h2>
        <p className="text-texto-suave">{textos.introDirecciones}</p>
        {direcciones.isSuccess && direcciones.data.length > 0 ? (
          <p className="text-sm text-texto-suave">{textos.conteo(direcciones.data.length)}</p>
        ) : null}
      </div>

      {aviso ? <p className="rounded-control border border-borde p-3 font-medium">{aviso}</p> : null}

      {direcciones.isPending ? (
        <EsqueletoLista filas={2} etiqueta={textos.cargandoDirecciones} />
      ) : direcciones.isError ? (
        <EstadoError error={direcciones.error} alReintentar={() => void direcciones.refetch()} />
      ) : (
        <>
          {direcciones.data.length === 0 && edicion?.tipo !== 'nueva' ? (
            <EstadoVacio titulo={textos.sinDirecciones}>{textos.sinDireccionesAyuda}</EstadoVacio>
          ) : (
            <ul className="flex flex-col gap-3">
              {direcciones.data.map((direccion) => (
                <li key={direccion.id} className="rounded-control border border-borde p-4">
                  {edicion?.tipo === 'editar' && edicion.id === direccion.id ? (
                    <FormularioDireccion
                      direccion={direccion}
                      telefonoSugerido={telefonoSugerido}
                      alGuardar={(nombre) => terminar(textos.direccionGuardada(nombre))}
                      alCancelar={() => setEdicion(null)}
                    />
                  ) : edicion?.tipo === 'borrar' && edicion.id === direccion.id ? (
                    <div className="flex flex-col gap-2">
                      <p className="font-medium">{textos.confirmarBorrar(direccion.nombre)}</p>
                      <div className="flex flex-wrap gap-2">
                        <Boton
                          type="button"
                          variante="peligro"
                          // eslint-disable-next-line jsx-a11y/no-autofocus
                          autoFocus
                          enviando={borrado.isPending}
                          textoEnviando={textos.botonBorrando}
                          onClick={() =>
                            borrado.mutate(direccion.id, {
                              onSuccess: () => terminar(textos.direccionBorrada(direccion.nombre)),
                            })
                          }
                        >
                          {textos.botonConfirmarBorrar}
                        </Boton>
                        <Boton type="button" variante="secundario" onClick={() => setEdicion(null)}>
                          {textos.botonCancelar}
                        </Boton>
                      </div>
                      {borrado.error ? <EstadoError error={borrado.error} /> : null}
                    </div>
                  ) : (
                    <TarjetaDireccion
                      direccion={direccion}
                      alEditar={() => {
                        setAviso(null)
                        setEdicion({ tipo: 'editar', id: direccion.id })
                      }}
                      alPedirBorrar={() => {
                        setAviso(null)
                        borrado.reset()
                        setEdicion({ tipo: 'borrar', id: direccion.id })
                      }}
                    />
                  )}
                </li>
              ))}
            </ul>
          )}

          {edicion?.tipo === 'nueva' ? (
            <FormularioDireccion
              telefonoSugerido={telefonoSugerido}
              alGuardar={(nombre) => terminar(textos.direccionGuardada(nombre))}
              alCancelar={() => setEdicion(null)}
            />
          ) : (
            <div className="flex flex-col gap-1">
              <Boton
                type="button"
                className="self-start"
                disabled={lleno}
                aria-describedby={lleno ? 'limite-direcciones' : undefined}
                onClick={() => {
                  setAviso(null)
                  setEdicion({ tipo: 'nueva' })
                }}
              >
                <Plus aria-hidden="true" className="size-5" />
                {textos.agregarDireccion}
              </Boton>
              {lleno ? (
                <p id="limite-direcciones" className="text-sm text-texto-suave">
                  {textos.limiteAlcanzado}
                </p>
              ) : null}
            </div>
          )}
        </>
      )}
    </section>
  )
}
