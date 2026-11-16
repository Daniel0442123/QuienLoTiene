package com.quienlotiene.clientes.api;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Copia de una dirección del cliente para entregarle una orden con domicilio (HU-024). */
public record DireccionEntrega(
        UUID direccionId,
        String nombre,
        String direccionEscrita,
        @Nullable String indicaciones,
        String telefono,
        double latitud,
        double longitud) {

    @Override
    public String toString() {
        // Dato personal (Ley 1581): no sale en logs
        return "DireccionEntrega[direccionId=" + direccionId + "]";
    }
}
