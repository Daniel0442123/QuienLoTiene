package com.quienlotiene.catalogo.domain.port;

import com.quienlotiene.catalogo.domain.model.Categoria;
import java.util.List;

/** Lee las categorías del esquema {@code catalogo}. */
public interface CategoriaRepository {

    List<Categoria> buscarActivas();
}
