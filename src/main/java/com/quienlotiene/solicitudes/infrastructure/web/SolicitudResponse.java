package com.quienlotiene.solicitudes.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Solicitud tal como la ve su cliente (HU-016, HU-020, HU-021).
 *
 * @param respuestasRecibidas todas, incluidas las NO (0 al publicar)
 * @param ordenId la orden creada al elegir una respuesta (RESUELTA)
 */
public record SolicitudResponse(
        UUID id,
        UUID categoriaId,
        String producto,
        int cantidad,
        @Schema(allowableValues = {"RECOGER", "DOMICILIO"}) String modalidad,
        boolean esUrgente,
        UbicacionResponse ubicacion,
        int radioEnMetros,

        @Schema(allowableValues = {"ABIERTA", "RESUELTA", "VENCIDA"})
        String estado,

        Instant fechaPublicacion,
        Instant fechaVencimiento,
        int tiendasNotificadas,
        int respuestasRecibidas,
        @Nullable UUID ordenId) {

    public record UbicacionResponse(double latitud, double longitud) {}
}
