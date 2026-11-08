package com.quienlotiene.identidad.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de {@code POST /api/v1/auth/login}. */
public record IniciarSesionRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Size(max = 254, message = "El correo puede tener máximo 254 caracteres")
        String correo,

        @NotBlank(message = "La frase de contraseña es obligatoria")
        @Size(max = 128, message = "La frase de contraseña puede tener máximo 128 caracteres")
        String fraseContrasena) {

    @Override
    public String toString() {
        return "IniciarSesionRequest[***]";
    }
}
