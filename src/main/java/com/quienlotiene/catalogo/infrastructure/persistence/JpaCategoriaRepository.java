package com.quienlotiene.catalogo.infrastructure.persistence;

import com.quienlotiene.catalogo.domain.model.Categoria;
import com.quienlotiene.catalogo.domain.port.CategoriaRepository;
import java.util.List;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link CategoriaRepository}. */
@Component
class JpaCategoriaRepository implements CategoriaRepository {

    private final CategoriaJpaRepository categoriaJpaRepository;

    JpaCategoriaRepository(CategoriaJpaRepository categoriaJpaRepository) {
        this.categoriaJpaRepository = categoriaJpaRepository;
    }

    @Override
    public List<Categoria> buscarActivas() {
        return categoriaJpaRepository.findByActivaTrue().stream()
                .map(fila -> new Categoria(fila.getId(), fila.getNombre(), fila.isActiva()))
                .toList();
    }
}
