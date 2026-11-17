package com.quienlotiene.clientes.infrastructure.web;

import com.quienlotiene.clientes.application.dto.DatosDireccion;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Una dirección guardada como la ve el cliente (HU-007). Siempre tiene id y fecha: a diferencia de
 * {@link DatosDireccion}, que también sirve para crearla, aquí no son nulables (DEF-006, C-04 A5). Conserva el nombre
 * {@code DatosDireccion} en el contrato para no cambiar los tipos del frontend.
 */
@Schema(name = "DatosDireccion")
record DireccionResponse(
        UUID id,
        String nombre,
        double latitud,
        double longitud,
        String direccionEscrita,
        @Nullable String indicaciones,
        String telefono,
        Instant fechaCreacion) {

    static DireccionResponse de(DatosDireccion datos) {
        return new DireccionResponse(
                Objects.requireNonNull(datos.id(), "una dirección guardada tiene id"),
                datos.nombre(),
                Objects.requireNonNull(datos.latitud(), "una dirección guardada tiene latitud"),
                Objects.requireNonNull(datos.longitud(), "una dirección guardada tiene longitud"),
                datos.direccionEscrita(),
                datos.indicaciones(),
                datos.telefono(),
                Objects.requireNonNull(datos.fechaCreacion(), "una dirección guardada tiene fecha"));
    }

    @Override
    public String toString() {
        // Una dirección es un dato personal (Ley 1581): no debe salir en logs
        return "DireccionResponse[id=" + id + "]";
    }
}
