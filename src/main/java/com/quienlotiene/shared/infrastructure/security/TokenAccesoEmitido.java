package com.quienlotiene.shared.infrastructure.security;

import java.time.Instant;

/**
 * Token de acceso firmado que se entrega al iniciar sesión.
 *
 * @param valor el JWT que el cliente envía en {@code Authorization: Bearer ...}
 * @param fechaExpiracion momento en que el token deja de ser válido
 */
public record TokenAccesoEmitido(String valor, Instant fechaExpiracion) {

    @Override
    public String toString() {
        // El token es una credencial: nunca debe salir completo en un log
        return "TokenAccesoEmitido[fechaExpiracion=" + fechaExpiracion + "]";
    }
}
