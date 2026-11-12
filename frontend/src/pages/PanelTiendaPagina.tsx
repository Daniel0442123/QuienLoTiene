import { BadgeCheck, ChevronRight, CircleAlert, Clock, Truck, Wallet } from 'lucide-react'
import { Link } from 'react-router'
import { EsqueletoLista, EstadoError } from '@/components/Estados'
import { useMiTienda, type MiTienda } from '@/features/tiendas/api'
import { InterruptorAperturaTienda } from '@/features/tiendas/InterruptorAperturaTienda'
import { textosTiendas as textos } from '@/features/tiendas/textos'

const SECCIONES = [
  { ruta: '/tienda/perfil', titulo: textos.seccionPerfil, descripcion: textos.descripcionPerfil, Icono: Clock },
  {
    ruta: '/tienda/domicilio',
    titulo: textos.seccionDomicilio,
    descripcion: textos.descripcionDomicilio,
    Icono: Truck,
  },
  {
    ruta: '/tienda/medios-pago',
    titulo: textos.seccionMediosPago,
    descripcion: textos.descripcionMediosPago,
    Icono: Wallet,
  },
]

function EstadoDeLaTienda({ tienda }: { tienda: MiTienda }) {
  const aprobada = tienda.estado === 'APROBADA'
  const Icono = aprobada ? BadgeCheck : CircleAlert
  return (
    <div className="flex flex-col gap-1">
      <h2 className="text-xl font-semibold">{tienda.nombre}</h2>
      <p
        className={
          aprobada
            ? 'flex items-center gap-2 font-medium text-exito'
            : 'flex items-center gap-2 font-medium text-advertencia'
        }
      >
        <Icono aria-hidden="true" className="size-5 shrink-0" />
        {textos.estadoTienda[tienda.estado]}
      </p>
      <p className="text-texto-suave">{textos.explicacionEstado[tienda.estado]}</p>
      {tienda.estado === 'RECHAZADA' && tienda.motivoRechazo ? (
        <p className="font-medium">{textos.motivoRechazo(tienda.motivoRechazo)}</p>
      ) : null}
    </div>
  )
}

/** /tienda · Panel de la tienda: estado, abrir o cerrar (HU-013) y acceso a la configuración (HU-009 a HU-012). */
export function PanelTiendaPagina() {
  const miTienda = useMiTienda()

  return (
    <section className="mx-auto flex max-w-xl flex-col gap-6">
      <h1 className="text-2xl font-bold">{textos.tituloPanel}</h1>

      {miTienda.isPending ? (
        <EsqueletoLista filas={3} etiqueta={textos.cargandoTienda} />
      ) : miTienda.isError ? (
        <EstadoError error={miTienda.error} alReintentar={() => void miTienda.refetch()} />
      ) : (
        <>
          <EstadoDeLaTienda tienda={miTienda.data} />
          <InterruptorAperturaTienda tienda={miTienda.data} />
          <nav aria-label={textos.navegacionPanel}>
            <ul className="flex flex-col gap-2">
              {SECCIONES.map(({ ruta, titulo, descripcion, Icono }) => (
                <li key={ruta}>
                  <Link
                    to={ruta}
                    className="flex min-h-tactil items-center gap-3 rounded-control border border-borde p-3 hover:bg-superficie"
                  >
                    <Icono aria-hidden="true" className="size-6 shrink-0 text-primario" />
                    <span className="flex flex-1 flex-col">
                      <span className="font-semibold">{titulo}</span>
                      <span className="text-sm text-texto-suave">{descripcion}</span>
                    </span>
                    <ChevronRight aria-hidden="true" className="size-5 shrink-0" />
                  </Link>
                </li>
              ))}
            </ul>
          </nav>
        </>
      )}
    </section>
  )
}
