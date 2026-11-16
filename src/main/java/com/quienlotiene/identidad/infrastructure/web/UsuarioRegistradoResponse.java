package com.quienlotiene.identidad.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/** Respuesta de un registro exitoso. Nunca incluye el hash ni la frase. */
public record UsuarioRegistradoResponse(
        UUID id,
        String nombre,
        String correo,

        @Schema(allowableValues = {"CLIENTE", "TIENDA", "ADMIN"})
        String rol,

        Instant fechaCreacion) {}
