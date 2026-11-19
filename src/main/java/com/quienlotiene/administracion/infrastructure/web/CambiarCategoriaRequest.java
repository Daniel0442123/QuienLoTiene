package com.quienlotiene.administracion.infrastructure.web;

import jakarta.validation.constraints.NotNull;

/** HU-038: {@code false} desactiva la categoría y {@code true} deshace. */
record CambiarCategoriaRequest(
        @NotNull(message = "Indica si la categoría queda activa")
        Boolean activa) {}
