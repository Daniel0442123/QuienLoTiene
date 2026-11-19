package com.quienlotiene.reputacion.infrastructure.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/** HU-034: estrellas de 1 a 5 y un comentario opcional. */
record CalificarRequest(
        @NotNull(message = "Elige de 1 a 5 estrellas")
        @Min(value = 1, message = "Elige de 1 a 5 estrellas")
        @Max(value = 5, message = "Elige de 1 a 5 estrellas")
        Integer estrellas,

        @Nullable @Size(max = 500, message = "El comentario puede tener hasta 500 caracteres")
        String comentario) {}
