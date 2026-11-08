package com.quienlotiene.administracion.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de {@code POST /api/v1/admin/tiendas/{id}/rechazo}. */
public record RechazarTiendaRequest(
        @NotBlank(message = "Escribe el motivo del rechazo")
        @Size(min = 5, max = 300, message = "El motivo debe tener entre 5 y 300 caracteres")
        String motivo) {}
