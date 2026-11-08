package com.quienlotiene.identidad.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta de iniciar o renovar la sesión. El token de refresco no va aquí: viaja solo en la cookie HttpOnly, para que
 * el JavaScript de la página no pueda leerlo.
 *
 * @param tipoToken siempre {@code Bearer}: el frontend envía {@code Authorization: Bearer <tokenAcceso>}
 */
public record SesionResponse(
        UUID usuarioId, String rol, String tokenAcceso, String tipoToken, Instant fechaExpiracion) {

    @Override
    public String toString() {
        return "SesionResponse[usuarioId=" + usuarioId + ", rol=" + rol + "]";
    }
}
