package com.quienlotiene.ordenes.domain.model;

/** Nombre y teléfono del cliente copiados al crear la orden (CP-ORD-024-02). */
public record ContactoCliente(String nombre, String telefono) {

    @Override
    public String toString() {
        return "ContactoCliente[***]";
    }
}
