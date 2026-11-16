package com.quienlotiene.ordenes.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

/** Cuerpo de {@code POST /api/v1/ordenes} (HU-024). La dirección es obligatoria con DOMICILIO (ORD-004). */
public record CrearOrdenRequest(
        @NotNull(message = "Elige una respuesta") UUID respuestaId,

        @NotNull(message = "Elige si vas a recoger o quieres domicilio")
        @Pattern(regexp = "RECOGER|DOMICILIO", message = "La modalidad debe ser RECOGER o DOMICILIO")
        @Schema(allowableValues = {"RECOGER", "DOMICILIO"})
        String modalidad,

        UUID direccionId,

        @NotNull(message = "Elige cómo vas a pagar")
        @Pattern(
                regexp = "EFECTIVO|TRANSFERENCIA|DATAFONO",
                message = "El medio de pago debe ser EFECTIVO, TRANSFERENCIA o DATAFONO")
        @Schema(allowableValues = {"EFECTIVO", "TRANSFERENCIA", "DATAFONO"})
        String medioPago) {}
