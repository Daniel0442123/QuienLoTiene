package com.quienlotiene.solicitudes.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Tarjeta de solicitud para la tienda (HU-018). Sin ningún dato del cliente (CP-SOL-018-01).
 *
 * @param distanciaEnMetros aproximada, redondeada a 100 m (CP-SOL-018-02)
 * @param precioSugeridoEnPesos último precio que la tienda dio para el mismo producto (SC-011)
 */
@Schema(name = "SolicitudDisponibleResponse")
public record SolicitudDisponible(
        UUID id,
        UUID categoriaId,
        String producto,
        int cantidad,
        @Schema(allowableValues = {"RECOGER", "DOMICILIO"}) String modalidad,
        boolean esUrgente,
        int distanciaEnMetros,
        Instant fechaPublicacion,
        Instant fechaVencimiento,
        @Nullable Long precioSugeridoEnPesos) {}
