package com.quienlotiene.reputacion.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/** HU-035: motivo de SC-011 y, con OTRO, qué pasó. */
record ReportarRequest(
        @NotBlank(message = "Elige el motivo del reporte")
        @Pattern(
                regexp = "DEMORA_ENTREGA|PRODUCTO_DIFERENTE|COBRO_DIFERENTE|OTRO",
                message = "El motivo debe ser DEMORA_ENTREGA, PRODUCTO_DIFERENTE, COBRO_DIFERENTE u OTRO")
        @Schema(allowableValues = {"DEMORA_ENTREGA", "PRODUCTO_DIFERENTE", "COBRO_DIFERENTE", "OTRO"})
        String motivo,

        @Nullable @Size(max = 1000, message = "La descripción puede tener hasta 1000 caracteres")
        String descripcion) {}
