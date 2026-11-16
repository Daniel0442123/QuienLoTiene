package com.quienlotiene.identidad.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta de iniciar o renovar la sesión. El token de refresco no va aquí: viaja solo en la cookie HttpOnly, para que
 * el JavaScript de la página no pueda leerlo.
 *
 * @param tipoToken siempre {@code Bearer}: el frontend envía {@code Authorization: Bearer <tokenAcceso>}
 * @param segundoFactorRequerido siempre {@code false}: la sesión ya está completa
 */
public record SesionResponse(
        UUID usuarioId,

        @Schema(allowableValues = {"CLIENTE", "TIENDA", "ADMIN"})
        String rol,

        String tokenAcceso,
        String tipoToken,
        Instant fechaExpiracion,
        boolean segundoFactorRequerido)
        implements RespuestaInicioSesion {

    @Override
    public String toString() {
        return "SesionResponse[usuarioId=" + usuarioId + ", rol=" + rol + "]";
    }
}
