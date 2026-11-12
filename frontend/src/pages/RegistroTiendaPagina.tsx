import { useEffect, useRef, useState } from 'react'
import { Link, Navigate } from 'react-router'
import { FormularioRegistroTienda } from '@/features/tiendas/FormularioRegistroTienda'
import { textosTiendas as textos } from '@/features/tiendas/textos'
import { useSesion } from '@/hooks/useSesion'

const claseEnlace = 'text-primario underline underline-offset-4 hover:no-underline'

/** /registrar-tienda · HU-002 y HU-005 (CU-15). Al terminar muestra que la tienda quedó en revisión (paso 12). */
export function RegistroTiendaPagina() {
  const sesion = useSesion()
  const [tiendaRegistrada, setTiendaRegistrada] = useState<string | null>(null)
  const tituloConfirmacion = useRef<HTMLHeadingElement>(null)

  // La pantalla cambia sin cambiar de ruta: el foco va al nuevo título para que el lector de pantalla lo lea
  useEffect(() => {
    if (tiendaRegistrada) {
      document.title = `${textos.tituloEnRevision} · ¿Quién lo tiene?`
      tituloConfirmacion.current?.focus()
    }
  }, [tiendaRegistrada])

  if (sesion) return <Navigate to="/" replace />

  if (tiendaRegistrada) {
    return (
      <section className="mx-auto flex max-w-md flex-col gap-4">
        <h1 ref={tituloConfirmacion} tabIndex={-1} className="text-2xl font-bold">
          {textos.tituloEnRevision}
        </h1>
        <p>{textos.textoEnRevision(tiendaRegistrada)}</p>
        <p className="text-texto-suave">{textos.textoMientrasTanto}</p>
        <Link
          to="/entrar"
          className="inline-flex min-h-tactil items-center self-start rounded-control bg-primario px-4 font-semibold text-primario-texto"
        >
          {textos.enlaceEntrar}
        </Link>
      </section>
    )
  }

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold">{textos.tituloRegistro}</h1>
        <p className="text-texto-suave">{textos.introRegistro}</p>
      </div>
      <FormularioRegistroTienda alRegistrar={setTiendaRegistrada} />
      <p>
        {textos.yaTienesCuenta}{' '}
        <Link to="/entrar" className={claseEnlace}>
          {textos.enlaceEntrar}
        </Link>
      </p>
    </section>
  )
}
