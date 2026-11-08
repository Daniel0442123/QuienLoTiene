package com.quienlotiene.tiendas.infrastructure.web;

import jakarta.validation.constraints.NotNull;

/** Cuerpo de {@code PATCH /api/v1/tiendas/me/apertura} (HU-013). */
public record CambiarAperturaRequest(
        @NotNull(message = "Indica si la tienda queda abierta o cerrada")
        Boolean abierta) {}
