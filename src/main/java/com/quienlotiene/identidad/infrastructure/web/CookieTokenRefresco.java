package com.quienlotiene.identidad.infrastructure.web;

import java.time.Duration;
import java.time.Instant;
import org.springframework.http.ResponseCookie;

/**
 * Cookie del token de refresco (D-15): {@code HttpOnly} para que el JavaScript no la lea, {@code Secure} para que solo
 * viaje por HTTPS (los navegadores aceptan {@code localhost} en desarrollo), {@code SameSite=Strict} para que otro
 * sitio no pueda usarla, y ruta {@value #RUTA} para que no viaje en el resto de peticiones.
 */
final class CookieTokenRefresco {

    static final String NOMBRE = "qlt_refresco";
    static final String RUTA = "/api/v1/auth";

    private CookieTokenRefresco() {}

    static ResponseCookie con(String tokenRefresco, Instant fechaExpiracion, Instant ahora) {
        return base(tokenRefresco)
                .maxAge(Duration.between(ahora, fechaExpiracion))
                .build();
    }

    /** Cookie vacía con duración cero: el navegador borra la que tenía. */
    static ResponseCookie borrada() {
        return base("").maxAge(Duration.ZERO).build();
    }

    private static ResponseCookie.ResponseCookieBuilder base(String valor) {
        return ResponseCookie.from(NOMBRE, valor)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(RUTA);
    }
}
