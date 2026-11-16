package com.quienlotiene.identidad.api;

import java.util.UUID;

/** Fachada para que ordenes copie el contacto del cliente al crear la orden (HU-024). */
public interface ContactoUsuariosApi {

    /**
     * @throws java.util.NoSuchElementException si el usuario no existe (no debería pasar: el id sale del token)
     */
    ContactoUsuario obtenerContacto(UUID usuarioId);
}
