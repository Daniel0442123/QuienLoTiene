package com.quienlotiene.catalogo.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Categoría de tienda y de solicitud (Farmacia, Ferretería...). Solo las activas se ofrecen a los usuarios. */
public record Categoria(UUID id, String nombre, boolean activa) {

    public Categoria {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(nombre, "nombre");
    }

    /** HU-038: la misma categoría activada o desactivada (CP-CAT-038-01). */
    public Categoria conActiva(boolean nuevaActiva) {
        return new Categoria(id, nombre, nuevaActiva);
    }
}
