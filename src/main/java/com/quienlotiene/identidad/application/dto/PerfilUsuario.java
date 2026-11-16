package com.quienlotiene.identidad.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Perfil del usuario que inició sesión (CU-03). */
public record PerfilUsuario(
        UUID id,
        String nombre,
        String correo,
        String telefono,

        @Schema(allowableValues = {"CLIENTE", "TIENDA", "ADMIN"})
        String rol) {

    @Override
    public String toString() {
        return "PerfilUsuario[id=" + id + ", rol=" + rol + "]";
    }
}
