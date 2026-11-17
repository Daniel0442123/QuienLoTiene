import { formatearPesos, formatearUnidades } from '@/services/formato'
import type { Orden } from './api'
import { textosOrdenes as textos } from './textos'

/**
 * Lo que se pidió y el total desglosado: producto, domicilio y total (CP-ORD-024-01). Lo ven el cliente y la tienda;
 * el texto del pago depende de quién lo lee (DEF-009).
 */
export function DetalleOrden({ orden, vista = 'cliente' }: { orden: Orden; vista?: 'cliente' | 'tienda' }) {
  return (
    <section aria-labelledby={`desglose-${orden.id}`} className="flex flex-col gap-2">
      <h2 id={`desglose-${orden.id}`} className="text-lg font-semibold">
        {textos.desglose}
      </h2>
      <dl className="flex flex-col gap-1 rounded-control bg-superficie p-4">
        <div className="flex justify-between gap-3">
          <dt className="break-words">
            {textos.producto(formatearPesos(orden.precioUnitarioEnPesos), formatearUnidades(orden.cantidad))}
          </dt>
          <dd>{formatearPesos(orden.subtotalEnPesos)}</dd>
        </div>
        <div className="flex justify-between gap-3">
          <dt>{textos.domicilio}</dt>
          <dd>
            {orden.modalidad === 'DOMICILIO' ? formatearPesos(orden.costoDomicilioEnPesos) : textos.domicilioNoAplica}
          </dd>
        </div>
        <div className="flex justify-between gap-3 border-t border-borde pt-2 font-bold">
          <dt>{textos.total}</dt>
          <dd>{formatearPesos(orden.totalEnPesos)}</dd>
        </div>
      </dl>
      <p className="text-texto-suave">
        {(vista === 'tienda' ? textos.pagoDelCliente : textos.pagoCon)(textos.mediosPago[orden.medioPago])}
      </p>
    </section>
  )
}
