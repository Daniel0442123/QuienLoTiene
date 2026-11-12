package com.quienlotiene.catalogo.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Categoría de tienda y de solicitud (Farmacia, Ferretería...). Solo las activas se ofrecen a los usuarios. */
public record Categoria(UUID id, String nombre, boolean activa) {

    public Categoria {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(nombre, "nombre");
    }
}
