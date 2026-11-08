package com.quienlotiene.identidad.infrastructure.web;

import java.time.Instant;

/**
 * El administrador escribió bien su frase y falta el código TOTP (CU-25, paso 6). El token temporal solo sirve para
 * {@code POST /api/v1/auth/totp}, vence a los 5 minutos y admite 5 intentos.
 *
 * @param segundoFactorRequerido siempre {@code true}
 */
public record SegundoFactorPendienteResponse(
        boolean segundoFactorRequerido, String tokenSegundoFactor, Instant fechaExpiracion)
        implements RespuestaInicioSesion {

    @Override
    public String toString() {
        return "SegundoFactorPendienteResponse[fechaExpiracion=" + fechaExpiracion + "]";
    }
}
