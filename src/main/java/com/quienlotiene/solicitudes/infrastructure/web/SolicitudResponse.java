package com.quienlotiene.solicitudes.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

/** Solicitud publicada, tal como la ve su cliente. */
public record SolicitudResponse(
        UUID id,
        UUID categoriaId,
        String producto,
        int cantidad,
        String modalidad,
        boolean esUrgente,
        UbicacionResponse ubicacion,
        int radioEnMetros,
        String estado,
        Instant fechaPublicacion,
        Instant fechaVencimiento) {

    public record UbicacionResponse(double latitud, double longitud) {}
}
