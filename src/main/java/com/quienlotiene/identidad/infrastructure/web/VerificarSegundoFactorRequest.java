package com.quienlotiene.identidad.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cuerpo de {@code POST /api/v1/auth/totp} (HU-004). */
public record VerificarSegundoFactorRequest(
        @NotBlank(message = "Falta el token del segundo factor")
        @Size(max = 64, message = "El token del segundo factor no es válido")
        String tokenSegundoFactor,

        @NotBlank(message = "Escribe el código de verificación")
        @Pattern(regexp = "\\d{6}", message = "El código tiene 6 dígitos")
        String codigo) {

    @Override
    public String toString() {
        return "VerificarSegundoFactorRequest[***]";
    }
}
