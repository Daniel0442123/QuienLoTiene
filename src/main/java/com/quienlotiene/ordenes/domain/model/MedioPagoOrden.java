package com.quienlotiene.ordenes.domain.model;

/** Cómo paga el cliente al recibir (HU-012: la app solo lo registra, no hay cobro en línea). */
public enum MedioPagoOrden {
    EFECTIVO,
    TRANSFERENCIA,
    DATAFONO
}
