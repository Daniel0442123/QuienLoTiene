package com.quienlotiene.administracion.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** HU-038: palabra que marca un producto como restringido, por ejemplo «amoxicilina». */
record AgregarRestringidoRequest(
        @NotBlank(message = "Escribe la palabra")
        @Size(min = 2, max = 80, message = "La palabra debe tener entre 2 y 80 caracteres")
        String palabraClave) {}
