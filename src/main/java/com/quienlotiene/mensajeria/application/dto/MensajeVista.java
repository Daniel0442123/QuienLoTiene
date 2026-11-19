package com.quienlotiene.mensajeria.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/**
 * Un mensaje del chat tal como lo ve quien consulta.
 *
 * @param autor quién lo escribió: el cliente o la tienda (sin nombres)
 * @param esMio {@code true} si lo escribió quien consulta
 */
public record MensajeVista(
        UUID id,
        @Schema(allowableValues = {"CLIENTE", "TIENDA"}) String autor,
        boolean esMio,
        String texto,
        Instant fecha) {}
