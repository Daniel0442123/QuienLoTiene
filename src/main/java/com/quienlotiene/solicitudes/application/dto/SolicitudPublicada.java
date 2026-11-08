package com.quienlotiene.solicitudes.application.dto;

import java.time.Instant;
import java.util.UUID;

/** Resultado de publicar una solicitud. */
public record SolicitudPublicada(
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
        Instant fechaVencimiento) {}
