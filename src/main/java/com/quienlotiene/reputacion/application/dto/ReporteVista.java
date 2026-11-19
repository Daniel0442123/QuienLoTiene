package com.quienlotiene.reputacion.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** El reporte como lo ve quien lo creó (HU-035). */
@Schema(name = "ReporteResponse")
public record ReporteVista(
        UUID id,
        UUID ordenId,

        @Schema(allowableValues = {"DEMORA_ENTREGA", "PRODUCTO_DIFERENTE", "COBRO_DIFERENTE", "OTRO"})
        String motivo,

        @Nullable String descripcion,
        @Schema(allowableValues = {"ABIERTO", "RESUELTO"}) String estado,
        Instant fecha) {}
