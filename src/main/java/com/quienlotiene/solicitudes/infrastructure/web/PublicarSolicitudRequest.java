package com.quienlotiene.solicitudes.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** Cuerpo de {@code POST /api/v1/solicitudes} (HU-016). CP-SOL-016-02: cada dato faltante sale en {@code errores}. */
public record PublicarSolicitudRequest(
        @NotBlank(message = "Escribe el producto que buscas")
        @Size(max = 150, message = "El producto puede tener máximo 150 caracteres")
        String producto,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 999, message = "La cantidad máxima es 999")
        Integer cantidad,

        @NotNull(message = "Elige la categoría") UUID categoriaId,

        @NotNull(message = "Elige si vas a recoger o quieres domicilio")
        @Pattern(regexp = "RECOGER|DOMICILIO", message = "La modalidad debe ser RECOGER o DOMICILIO")
        String modalidad,

        Boolean esUrgente,

        @NotNull(message = "Comparte tu ubicación") @Valid UbicacionRequest ubicacion) {

    /** Coordenadas en grados decimales, como las entrega el GPS. */
    public record UbicacionRequest(
            @NotNull(message = "Falta la latitud")
            @DecimalMin(value = "-90", message = "La latitud debe estar entre -90 y 90")
            @DecimalMax(value = "90", message = "La latitud debe estar entre -90 y 90")
            Double latitud,

            @NotNull(message = "Falta la longitud")
            @DecimalMin(value = "-180", message = "La longitud debe estar entre -180 y 180")
            @DecimalMax(value = "180", message = "La longitud debe estar entre -180 y 180")
            Double longitud) {}
}
