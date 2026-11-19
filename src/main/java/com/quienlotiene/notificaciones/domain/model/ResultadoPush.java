package com.quienlotiene.notificaciones.domain.model;

/** Lo que respondió el servicio push del navegador (diagrama 28, pasos 10 a 14). */
public enum ResultadoPush {
    ENTREGADO,
    /** 404 o 410: el navegador ya no tiene esa suscripción y hay que borrarla. */
    VENCIDA,
    FALLO
}
