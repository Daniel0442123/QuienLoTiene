package com.quienlotiene.tiendas.infrastructure.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Cuerpo de {@code PUT /api/v1/tiendas/me/domicilio} (HU-010). Con {@code haceDomicilio} en {@code false} los demás
 * campos se ignoran.
 */
public record ConfigurarDomicilioRequest(
        @NotNull(message = "Indica si la tienda hace domicilios")
        Boolean haceDomicilio,

        @Min(value = 500, message = "El radio de cobertura mínimo es 500 metros")
        @Max(value = 10_000, message = "El radio de cobertura máximo es 10.000 metros")
        Integer radioCoberturaEnMetros,

        @PositiveOrZero(message = "La tarifa no puede ser negativa")
        Long tarifaFijaEnPesos) {}
