package com.quienlotiene.solicitudes.application.dto;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * La solicitud como la ve su cliente (HU-020, HU-021).
 *
 * @param respuestasRecibidas todas, incluidas las NO
 * @param ordenId la orden creada al elegir (RESUELTA)
 */
public record SolicitudDelCliente(
        UUID id,
        UUID categoriaId,
        String producto,
        int cantidad,
        String modalidad,
        boolean esUrgente,
        double latitud,
        double longitud,
        int radioEnMetros,
        String estado,
        Instant fechaPublicacion,
        Instant fechaVencimiento,
        int tiendasNotificadas,
        int respuestasRecibidas,
        @Nullable UUID ordenId) {

    @Override
    public String toString() {
        return "SolicitudDelCliente[id=" + id + "]";
    }
}
