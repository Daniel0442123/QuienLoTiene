package com.quienlotiene.ordenes.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** HU-029: por qué se cancela; le llega a la otra parte en el aviso. */
record CancelarOrdenRequest(
        @NotBlank(message = "Escribe el motivo de la cancelación")
        @Size(min = 3, max = 200, message = "El motivo debe tener entre 3 y 200 caracteres")
        String motivo) {}
