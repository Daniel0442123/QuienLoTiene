package com.quienlotiene.tiendas.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/** Resultado del registro: la tienda queda PENDIENTE de revisión. */
public record TiendaRegistrada(
        UUID id,
        String nombre,

        @Schema(allowableValues = {"PENDIENTE", "APROBADA", "RECHAZADA", "SUSPENDIDA"})
        String estado,

        Instant fechaRegistro) {}
