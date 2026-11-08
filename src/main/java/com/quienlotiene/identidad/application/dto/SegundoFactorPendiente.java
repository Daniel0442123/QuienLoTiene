package com.quienlotiene.identidad.application.dto;

import java.time.Instant;

/**
 * La frase del administrador fue correcta y falta el código TOTP (HU-004).
 *
 * @param tokenSegundoFactor token temporal que solo sirve para {@code POST /api/v1/auth/totp}
 */
public record SegundoFactorPendiente(String tokenSegundoFactor, Instant fechaExpiracion)
        implements ResultadoInicioSesion {

    @Override
    public String toString() {
        return "SegundoFactorPendiente[fechaExpiracion=" + fechaExpiracion + "]";
    }
}
