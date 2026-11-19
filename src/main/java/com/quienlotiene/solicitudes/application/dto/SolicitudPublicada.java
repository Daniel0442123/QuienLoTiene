package com.quienlotiene.solicitudes.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Resultado de publicar una solicitud.
 *
 * @param esRestringida HU-023: el cliente ve el aviso «Este medicamento necesita fórmula médica» y no puede pedir
 *     domicilio
 */
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
        Instant fechaVencimiento,
        int tiendasNotificadas,
        boolean esRestringida) {}
