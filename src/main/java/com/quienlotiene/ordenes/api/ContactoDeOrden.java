package com.quienlotiene.ordenes.api;

import java.util.UUID;

/**
 * La otra parte de una orden, para contactarla (HU-032): para el cliente, la tienda; para la tienda, el cliente.
 *
 * @param esActiva solo con la orden activa se puede mostrar el teléfono
 */
public record ContactoDeOrden(UUID ordenId, boolean esActiva, String nombre, String telefono, String producto) {

    @Override
    public String toString() {
        return "ContactoDeOrden[ordenId=" + ordenId + "]";
    }
}
