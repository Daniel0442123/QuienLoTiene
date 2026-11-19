package com.quienlotiene.catalogo.domain.port;

import com.quienlotiene.catalogo.domain.model.Categoria;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Lee y actualiza las categorías del esquema {@code catalogo}. */
public interface CategoriaRepository {

    List<Categoria> buscarActivas();

    /** HU-038: activas e inactivas. */
    List<Categoria> buscarTodas();

    Optional<Categoria> buscarPorId(UUID categoriaId);

    /** HU-038: guarda si la categoría está activa. */
    void guardarActiva(Categoria categoria);
}
