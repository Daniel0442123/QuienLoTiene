package com.quienlotiene.solicitudes.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Una solicitud en el historial del cliente (HU-008 y SC-018 «Mis preguntas»). Trae lo necesario para repetirla y
 * para volver a sus respuestas, sin la ubicación (cada repetición usa la ubicación del momento).
 *
 * @param estaAbierta {@code true} si sigue ABIERTA y no ha vencido: la que muestra «Mis preguntas»
 * @param respuestasRecibidas todas, incluidas las NO
 * @param ordenId la orden creada al elegir (RESUELTA)
 */
public record SolicitudDelHistorial(
        UUID id,
        UUID categoriaId,
        String producto,
        int cantidad,
        @Schema(allowableValues = {"RECOGER", "DOMICILIO"}) String modalidad,
        boolean esUrgente,

        @Schema(allowableValues = {"ABIERTA", "RESUELTA", "VENCIDA"})
        String estado,

        boolean estaAbierta,
        Instant fechaPublicacion,
        Instant fechaVencimiento,
        int respuestasRecibidas,
        @Nullable UUID ordenId,
        boolean esRestringida) {}
