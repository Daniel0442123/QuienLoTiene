package com.quienlotiene.ordenes.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Cuerpo de {@code PATCH /api/v1/ordenes/{id}/estado} (HU-027). */
public record CambiarEstadoOrdenRequest(
        @NotNull(message = "Elige el estado nuevo")
        @Pattern(
                regexp = "PREPARANDO|EN_CAMINO|LISTO_PARA_RECOGER|ENTREGADO",
                message = "El estado debe ser PREPARANDO, EN_CAMINO, LISTO_PARA_RECOGER o ENTREGADO")
        @Schema(allowableValues = {"PREPARANDO", "EN_CAMINO", "LISTO_PARA_RECOGER", "ENTREGADO"})
        String estado) {}
