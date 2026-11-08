package com.quienlotiene.identidad.application.dto;

import java.util.UUID;

/** Perfil del usuario que inició sesión (CU-03). */
public record PerfilUsuario(UUID id, String nombre, String correo, String telefono, String rol) {

    @Override
    public String toString() {
        return "PerfilUsuario[id=" + id + ", rol=" + rol + "]";
    }
}
