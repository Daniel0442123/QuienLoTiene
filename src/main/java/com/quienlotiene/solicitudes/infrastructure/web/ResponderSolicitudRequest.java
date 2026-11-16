package com.quienlotiene.solicitudes.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de {@code POST /api/v1/solicitudes/{id}/respuestas} (HU-019). Qué datos pide cada tipo lo revisa el dominio
 * (SOL-006): precio y unidades con SI y PARECIDO, descripción con PARECIDO.
 */
public record ResponderSolicitudRequest(
        @NotNull(message = "Elige Sí lo tengo, Tengo algo parecido o No lo tengo")
        @Pattern(regexp = "SI|PARECIDO|NO", message = "El tipo debe ser SI, PARECIDO o NO")
        @Schema(allowableValues = {"SI", "PARECIDO", "NO"})
        String tipo,

        @Min(value = 1, message = "El precio debe ser mayor que 0")
        @Max(value = 100_000_000, message = "El precio máximo es 100.000.000")
        Long precioEnPesos,

        @Min(value = 1, message = "Las unidades deben ser al menos 1")
        @Max(value = 999, message = "Las unidades máximas son 999")
        Integer cantidadDisponible,

        @Size(max = 300, message = "La descripción puede tener máximo 300 caracteres")
        String descripcionAlternativa,

        @Min(value = 1, message = "El tiempo estimado debe ser al menos 1 minuto")
        @Max(value = 240, message = "El tiempo estimado máximo es 240 minutos")
        Integer tiempoEstimadoEnMinutos) {}
