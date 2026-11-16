package com.quienlotiene.ordenes.domain.model;

/** Copia de la dirección del cliente al crear la orden con domicilio. La tienda la ve solo con la orden activa. */
public record DireccionDeEntrega(
        String nombre, String direccionEscrita, String indicaciones, double latitud, double longitud) {

    @Override
    public String toString() {
        return "DireccionDeEntrega[***]";
    }
}
