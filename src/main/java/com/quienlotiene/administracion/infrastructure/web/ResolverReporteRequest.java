package com.quienlotiene.administracion.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** HU-037: qué se hizo con el reporte. */
record ResolverReporteRequest(
        @NotBlank(message = "Escribe la nota de resolución")
        @Size(min = 3, max = 500, message = "La nota debe tener entre 3 y 500 caracteres")
        String nota) {}
