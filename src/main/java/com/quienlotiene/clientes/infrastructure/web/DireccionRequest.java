package com.quienlotiene.clientes.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cuerpo de {@code POST} y {@code PUT /api/v1/clientes/me/direcciones} (HU-007). */
public record DireccionRequest(
        @NotBlank(message = "Ponle un nombre a la dirección, por ejemplo Casa")
        @Size(max = 40, message = "El nombre puede tener máximo 40 caracteres")
        String nombre,

        @NotNull(message = "Marca la ubicación en el mapa") @Valid
        UbicacionRequest ubicacion,

        @NotBlank(message = "Escribe la dirección")
        @Size(max = 200, message = "La dirección puede tener máximo 200 caracteres")
        String direccionEscrita,

        @Size(max = 200, message = "Las indicaciones pueden tener máximo 200 caracteres")
        String indicaciones,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(
                regexp = "^\\+?[0-9]{7,15}$",
                message = "El teléfono debe tener entre 7 y 15 dígitos, con + opcional al inicio")
        String telefono) {

    /** Punto marcado en el mapa (OpenStreetMap), en grados decimales. */
    public record UbicacionRequest(
            @NotNull(message = "Falta la latitud")
            @DecimalMin(value = "-90", message = "La latitud debe estar entre -90 y 90")
            @DecimalMax(value = "90", message = "La latitud debe estar entre -90 y 90")
            Double latitud,

            @NotNull(message = "Falta la longitud")
            @DecimalMin(value = "-180", message = "La longitud debe estar entre -180 y 180")
            @DecimalMax(value = "180", message = "La longitud debe estar entre -180 y 180")
            Double longitud) {}

    @Override
    public String toString() {
        return "DireccionRequest[***]";
    }
}
