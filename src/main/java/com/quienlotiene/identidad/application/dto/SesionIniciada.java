package com.quienlotiene.identidad.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Resultado de iniciar o refrescar la sesión. El token de refresco va en texto plano solo para ponerlo en la cookie;
 * en la base queda su hash.
 */
public record SesionIniciada(
        UUID usuarioId,
        String rol,
        String tokenAcceso,
        Instant fechaExpiracionAcceso,
        String tokenRefresco,
        Instant fechaExpiracionRefresco) {

    @Override
    public String toString() {
        return "SesionIniciada[usuarioId=" + usuarioId + ", rol=" + rol + "]";
    }
}
