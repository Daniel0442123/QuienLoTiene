import { Route, Routes } from 'react-router'
import { base, type EstadoOrdenSimulado, type OrdenSimulada } from '@/mocks/datos'
import { PedidoPagina } from '@/pages/PedidoPagina'
import { PedidoTiendaPagina } from '@/pages/PedidoTiendaPagina'
import { renderizarConProveedores } from './renderizar'

/** Ayudas de las pruebas del Sprint 3 sobre órdenes: las dos órdenes de la simulación, con Droguería La 5. */

/** De la clienta de prueba, para recoger. */
export const ORDEN_DE_LA_CLIENTA = 'e1f2a3b4-0002-4c5d-8e9f-0a1b2c3d4e02'
/** De la vecina, a domicilio. */
export const ORDEN_DE_LA_VECINA = 'e1f2a3b4-0001-4c5d-8e9f-0a1b2c3d4e01'

const MINUTO = 60_000
const PASOS: Record<OrdenSimulada['modalidad'], EstadoOrdenSimulado[]> = {
  RECOGER: ['CONFIRMADO', 'PREPARANDO', 'LISTO_PARA_RECOGER', 'ENTREGADO'],
  DOMICILIO: ['CONFIRMADO', 'PREPARANDO', 'EN_CAMINO', 'ENTREGADO'],
}

export const ordenEnBase = (ordenId: string) => base.ordenes.find((o) => o.id === ordenId)!

/**
 * Deja la orden en un estado de su recorrido, con un paso por minuto en el historial que termina ahora. En
 * LISTO_PARA_RECOGER la reserva vence `minutosDeReserva` después de ahora (30 por defecto, SC-019).
 */
export function ponerOrdenEn(ordenId: string, estado: EstadoOrdenSimulado, minutosDeReserva = 30): OrdenSimulada {
  const orden = ordenEnBase(ordenId)
  const pasos = PASOS[orden.modalidad]
  const hasta = pasos.indexOf(estado)
  const recorrido = hasta >= 0 ? pasos.slice(0, hasta + 1) : [...pasos.slice(0, 1), estado]
  const ahora = Date.now()
  orden.estado = estado
  orden.historial = recorrido.map((paso, i) => ({
    estado: paso,
    fecha: new Date(ahora - (recorrido.length - 1 - i) * MINUTO).toISOString(),
  }))
  orden.reservaVence =
    estado === 'LISTO_PARA_RECOGER' ? new Date(ahora + minutosDeReserva * MINUTO).toISOString() : null
  return orden
}

export function abrirPedidoCliente(ordenId: string) {
  return renderizarConProveedores(
    <Routes>
      <Route path="/pedidos/:ordenId" element={<PedidoPagina />} />
    </Routes>,
    { ruta: `/pedidos/${ordenId}` },
  )
}

export function abrirPedidoTienda(ordenId: string) {
  return renderizarConProveedores(
    <Routes>
      <Route path="/tienda/pedidos/:ordenId" element={<PedidoTiendaPagina />} />
    </Routes>,
    { ruta: `/tienda/pedidos/${ordenId}` },
  )
}
