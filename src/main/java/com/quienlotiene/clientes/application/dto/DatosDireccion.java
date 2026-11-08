package com.quienlotiene.clientes.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Dirección como la envía y la ve el cliente (HU-007).
 *
 * @param id {@code null} al crearla
 */
public record DatosDireccion(
        UUID id,
        String nombre,
        Double latitud,
        Double longitud,
        String direccionEscrita,
        String indicaciones,
        String telefono,
        Instant fechaCreacion) {

    @Override
    public String toString() {
        // Una dirección es un dato personal (Ley 1581): no debe salir en logs
        return "DatosDireccion[id=" + id + "]";
    }
}
