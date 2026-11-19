package com.quienlotiene.reputacion.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Un reporte en el panel del administrador (HU-037). Sin datos personales de quien reporta: solo si fue el cliente o la
 * tienda.
 *
 * @param reportesDeLaTienda cuántos reportes tiene en total la tienda de la orden
 */
public record ReporteParaAdministrador(
        UUID id,
        UUID ordenId,
        UUID tiendaId,
        @Schema(allowableValues = {"CLIENTE", "TIENDA"}) String reportadoPor,

        @Schema(allowableValues = {"DEMORA_ENTREGA", "PRODUCTO_DIFERENTE", "COBRO_DIFERENTE", "OTRO"})
        String motivo,

        @Nullable String descripcion,
        @Schema(allowableValues = {"ABIERTO", "RESUELTO"}) String estado,
        Instant fecha,
        @Nullable String notaResolucion,
        @Nullable Instant fechaResolucion,
        long reportesDeLaTienda) {}
