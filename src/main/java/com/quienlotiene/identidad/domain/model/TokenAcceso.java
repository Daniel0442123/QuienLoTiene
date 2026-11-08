package com.quienlotiene.identidad.domain.model;

import java.time.Instant;

/** Token de acceso firmado (JWT de 15 minutos) que el cliente envía en cada petición. */
public record TokenAcceso(String valor, Instant fechaExpiracion) {

    @Override
    public String toString() {
        return "TokenAcceso[fechaExpiracion=" + fechaExpiracion + "]";
    }
}
