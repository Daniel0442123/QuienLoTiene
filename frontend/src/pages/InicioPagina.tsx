import {
  ChevronRight,
  CircleUserRound,
  Flag,
  History,
  MessageCircleQuestion,
  Package,
  ShoppingBag,
  Store,
  Tags,
  Inbox,
  type LucideIcon,
} from 'lucide-react'
import { Link } from 'react-router'
import { textosAdministracion } from '@/features/administracion/textos'
import { textosClientes } from '@/features/clientes/textos'
import { textosIdentidad } from '@/features/identidad/textos'
import { textosInicio as textos } from '@/features/inicio/textos'
import { textosOrdenes } from '@/features/ordenes/textos'
import { textosSolicitudes } from '@/features/solicitudes/textos'
import { textosTiendas } from '@/features/tiendas/textos'
import { useSesion } from '@/hooks/useSesion'

const CLASE_BOTON_PRINCIPAL =
  'inline-flex min-h-tactil items-center self-start rounded-control bg-primario px-4 font-semibold text-primario-texto hover:bg-primario-hover'
const CLASE_BOTON_SECUNDARIO =
  'inline-flex min-h-tactil items-center rounded-control border border-borde bg-fondo px-4 font-semibold hover:bg-superficie'

interface Acceso {
  a: string
  titulo: string
  descripcion: string
  Icono: LucideIcon
}

/** Tarjetas de acceso rápido: el mismo estilo de las secciones del panel de la tienda. */
function AccesosRapidos({ accesos }: { accesos: Acceso[] }) {
  return (
    <section aria-labelledby="titulo-accesos" className="flex flex-col gap-3">
      <h2 id="titulo-accesos" className="text-xl font-semibold">
        {textos.tituloAccesos}
      </h2>
      <ul className="grid gap-3 sm:grid-cols-2">
        {accesos.map(({ a, titulo, descripcion, Icono }) => (
          <li key={a}>
            <Link
              to={a}
              className="flex h-full min-h-tactil items-center gap-3 rounded-control border border-borde p-4 hover:bg-superficie"
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
    </section>
  )
}

const ICONOS_PASOS = [MessageCircleQuestion, Store, ShoppingBag]

function ComoFunciona() {
  return (
    <section aria-labelledby="titulo-como-funciona" className="flex flex-col gap-3">
      <h2 id="titulo-como-funciona" className="text-xl font-semibold">
        {textos.tituloComoFunciona}
      </h2>
      <ol className="grid gap-3 sm:grid-cols-3">
        {textos.pasos.map((paso, indice) => {
          const Icono = ICONOS_PASOS[indice] ?? MessageCircleQuestion
          return (
            <li key={paso.titulo} className="flex flex-col gap-2 rounded-control bg-superficie p-4">
              <span className="flex items-center gap-2">
                <span
                  aria-hidden="true"
                  className="flex size-8 shrink-0 items-center justify-center rounded-full bg-primario font-bold text-primario-texto"
                >
                  {indice + 1}
                </span>
                <Icono aria-hidden="true" className="size-6 text-primario" />
              </span>
              <h3 className="font-semibold">
                <span className="sr-only">{`Paso ${indice + 1}: `}</span>
                {paso.titulo}
              </h3>
              <p className="text-texto-suave">{paso.descripcion}</p>
            </li>
          )
        })}
      </ol>
    </section>
  )
}

function InicioSinSesion() {
  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-4">
        <h1 className="text-3xl font-bold">{textos.titulo}</h1>
        <p className="text-lg">{textos.lema}</p>
        <p className="text-texto-suave">{textos.explicacion}</p>
        <div className="flex flex-wrap gap-3">
          <Link to="/crear-cuenta" className={CLASE_BOTON_PRINCIPAL}>
            {textosIdentidad.enlaceCrearCuenta}
          </Link>
          <Link to="/entrar" className={CLASE_BOTON_SECUNDARIO}>
            {textosIdentidad.enlaceEntrar}
          </Link>
        </div>
      </section>
      <ComoFunciona />
      <section aria-labelledby="titulo-tiendas" className="flex flex-col gap-2 rounded-control border border-borde p-4">
        <h2 id="titulo-tiendas" className="flex items-center gap-2 text-xl font-semibold">
          <Store aria-hidden="true" className="size-6 shrink-0 text-primario" />
          {textosTiendas.invitacionInicio}
        </h2>
        <p>{textos.descripcionTiendas}</p>
        <p>
          <Link
            to="/registrar-tienda"
            className="inline-flex min-h-tactil items-center font-semibold text-primario underline underline-offset-4 hover:no-underline"
          >
            {textosTiendas.enlaceRegistrarTienda}
          </Link>
        </p>
      </section>
    </div>
  )
}

function InicioCliente() {
  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-4">
        <h1 className="text-2xl font-bold">{textos.tituloCliente}</h1>
        <p>{textos.introCliente}</p>
        <Link to="/solicitudes/nueva" className={CLASE_BOTON_PRINCIPAL}>
          {textosSolicitudes.enlacePreguntar}
        </Link>
      </section>
      <AccesosRapidos
        accesos={[
          {
            a: '/solicitudes',
            titulo: textos.enlaceMisPreguntas,
            descripcion: textos.descripcionMisPreguntas,
            Icono: History,
          },
          {
            a: '/pedidos',
            titulo: textosOrdenes.enlaceMisPedidos,
            descripcion: textos.descripcionMisPedidos,
            Icono: Package,
          },
          {
            a: '/cuenta',
            titulo: textosClientes.enlaceMiCuenta,
            descripcion: textos.descripcionMiCuenta,
            Icono: CircleUserRound,
          },
        ]}
      />
      <ComoFunciona />
    </div>
  )
}

function InicioTienda() {
  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-4">
        <h1 className="text-2xl font-bold">{textos.tituloTienda}</h1>
        <p>{textos.introTienda}</p>
        <Link to="/tienda" className={CLASE_BOTON_PRINCIPAL}>
          {textosTiendas.enlacePanel}
        </Link>
      </section>
      <AccesosRapidos
        accesos={[
          {
            a: '/tienda/solicitudes',
            titulo: textosSolicitudes.seccionSolicitudes,
            descripcion: textosSolicitudes.descripcionSeccionSolicitudes,
            Icono: Inbox,
          },
          {
            a: '/tienda/pedidos',
            titulo: textosOrdenes.seccionPedidos,
            descripcion: textosOrdenes.descripcionSeccionPedidos,
            Icono: Package,
          },
        ]}
      />
    </div>
  )
}

function InicioAdmin() {
  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-4">
        <h1 className="text-2xl font-bold">{textos.tituloAdmin}</h1>
        <p>{textos.introAdmin}</p>
      </section>
      <AccesosRapidos
        accesos={[
          {
            a: '/admin/tiendas',
            titulo: textosAdministracion.enlaceTiendas,
            descripcion: textos.descripcionTiendasAdmin,
            Icono: Store,
          },
          {
            a: '/admin/reportes',
            titulo: textosAdministracion.enlaceReportes,
            descripcion: textos.descripcionReportesAdmin,
            Icono: Flag,
          },
          {
            a: '/admin/catalogo',
            titulo: textosAdministracion.enlaceCatalogo,
            descripcion: textos.descripcionCatalogoAdmin,
            Icono: Tags,
          },
        ]}
      />
    </div>
  )
}

/** / · Inicio: qué hace la app para quien llega, y accesos rápidos según el rol para quien ya entró (SC-020). */
export function InicioPagina() {
  const sesion = useSesion()
  if (!sesion) return <InicioSinSesion />
  if (sesion.rol === 'TIENDA') return <InicioTienda />
  if (sesion.rol === 'ADMIN') return <InicioAdmin />
  return <InicioCliente />
}
