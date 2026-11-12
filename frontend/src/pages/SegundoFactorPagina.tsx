import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { FormularioSegundoFactor } from '@/features/identidad/FormularioSegundoFactor'
import { textosIdentidad as textos } from '@/features/identidad/textos'
import { descartarSegundoFactorPendiente, haySegundoFactorPendiente } from '@/services/sesion'

/**
 * /entrar/codigo · HU-004 (CU-25 pasos 7 a 17). Solo tiene sentido justo después de escribir la frase:
 * el token temporal vive en memoria, así que al recargar o entrar directo se vuelve a /entrar.
 */
export function SegundoFactorPagina() {
  const navegar = useNavigate()
  const { state } = useLocation()
  // Sin un destino concreto ("/" es el de cualquier inicio de sesión), el administrador llega a revisar tiendas
  const pedido = (state as { volverA?: string } | null)?.volverA
  const volverA = pedido && pedido !== '/' ? pedido : '/admin/tiendas'
  // Se revisa una sola vez: si luego vence (IDE-013), la pantalla lo explica en vez de saltar sin aviso
  const [habiaPendiente] = useState(() => haySegundoFactorPendiente())

  if (!habiaPendiente) return <Navigate to="/entrar" replace />

  return (
    <section className="mx-auto flex max-w-md flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold">{textos.tituloSegundoFactor}</h1>
        <p>{textos.introSegundoFactor}</p>
        <p className="text-texto-suave">{textos.limiteSegundoFactor}</p>
      </div>
      <FormularioSegundoFactor alVerificar={() => navegar(volverA, { replace: true })} />
      <Link
        to="/entrar"
        replace
        onClick={descartarSegundoFactorPendiente}
        className="inline-flex min-h-tactil items-center self-start text-primario underline underline-offset-4 hover:no-underline"
      >
        {textos.enlaceUsarOtraCuenta}
      </Link>
    </section>
  )
}
