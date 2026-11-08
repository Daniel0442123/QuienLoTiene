package com.quienlotiene.tiendas.domain.model;

/** Formas de pago que la tienda acepta al entregar (HU-012). La app solo las registra: no hay cobro en línea. */
public enum MedioPago {
    EFECTIVO,
    TRANSFERENCIA,
    DATAFONO
}
