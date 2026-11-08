package com.quienlotiene.tiendas.domain.model;

/** Estados de la tienda (diagrama 05-estados). Nace PENDIENTE; solo una tienda APROBADA recibe solicitudes. */
public enum EstadoTienda {
    PENDIENTE,
    APROBADA,
    RECHAZADA,
    SUSPENDIDA
}
