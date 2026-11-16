package com.quienlotiene.identidad.api;

import java.util.UUID;

/** Nombre y teléfono de un usuario, para que la tienda elegida pueda atender la orden (CP-ORD-024-02). */
public record ContactoUsuario(UUID usuarioId, String nombre, String telefono) {

    @Override
    public String toString() {
        return "ContactoUsuario[usuarioId=" + usuarioId + "]";
    }
}
