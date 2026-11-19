package com.quienlotiene.administracion.domain.model;

/** Acciones del administrador que quedan en la auditoría (RNF-013). */
public enum AccionAuditada {
    TIENDA_APROBADA,
    TIENDA_RECHAZADA,
    DOCUMENTO_TIENDA_DESCARGADO,
    CATEGORIA_ACTIVADA,
    CATEGORIA_DESACTIVADA,
    PRODUCTO_RESTRINGIDO_AGREGADO,
    PRODUCTO_RESTRINGIDO_QUITADO,
    REPORTE_RESUELTO,
    TIENDA_SUSPENDIDA,
    TIENDA_REACTIVADA
}
