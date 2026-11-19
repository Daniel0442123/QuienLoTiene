package com.quienlotiene.administracion.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** HU-037: por qué se suspende; queda en la auditoría. */
record SuspenderTiendaRequest(
        @NotBlank(message = "Escribe el motivo de la suspensión")
        @Size(min = 3, max = 300, message = "El motivo debe tener entre 3 y 300 caracteres")
        String motivo) {}
