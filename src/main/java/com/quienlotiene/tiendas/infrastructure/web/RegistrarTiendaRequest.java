package com.quienlotiene.tiendas.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Parte {@code datos} (JSON) de {@code POST /api/v1/tiendas/registro}. Los documentos llegan en las partes
 * {@code fotoLocal} y {@code rut} del mismo multipart.
 */
public record RegistrarTiendaRequest(
        @NotBlank(message = "Escribe tu nombre")
        @Size(max = 120, message = "El nombre puede tener máximo 120 caracteres")
        String nombreDueno,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Size(max = 254, message = "El correo puede tener máximo 254 caracteres")
        String correo,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(
                regexp = "^\\+?[0-9]{7,15}$",
                message = "El teléfono debe tener entre 7 y 15 dígitos, con + opcional al inicio")
        String telefono,

        @NotBlank(message = "La frase de contraseña es obligatoria")
        @Size(max = 128, message = "La frase de contraseña puede tener máximo 128 caracteres")
        String fraseContrasena,

        @NotNull(message = "Debes aceptar la política de tratamiento de datos")
        @AssertTrue(message = "Debes aceptar la política de tratamiento de datos")
        Boolean aceptaPoliticaDatos,

        @NotBlank(message = "La versión de la política aceptada es obligatoria")
        @Size(max = 10, message = "La versión de la política puede tener máximo 10 caracteres")
        String versionPoliticaDatos,

        @NotBlank(message = "Escribe el nombre de la tienda")
        @Size(max = 120, message = "El nombre de la tienda puede tener máximo 120 caracteres")
        String nombreTienda,

        @NotNull(message = "Elige la categoría de la tienda")
        UUID categoriaId,

        @NotNull(message = "Marca la ubicación del local en el mapa") @Valid
        UbicacionRequest ubicacion) {

    /** Coordenadas del local en grados decimales. */
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
        return "RegistrarTiendaRequest[***]";
    }
}
