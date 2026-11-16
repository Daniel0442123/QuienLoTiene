package com.quienlotiene.solicitudes.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/** Solicitud publicada, tal como la ve su cliente. */
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
        int tiendasNotificadas) {

    public record UbicacionResponse(double latitud, double longitud) {}
}
