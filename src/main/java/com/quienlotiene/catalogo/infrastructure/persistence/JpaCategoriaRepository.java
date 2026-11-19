package com.quienlotiene.catalogo.infrastructure.persistence;

import com.quienlotiene.catalogo.domain.model.Categoria;
import com.quienlotiene.catalogo.domain.port.CategoriaRepository;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link CategoriaRepository}. */
@Component
class JpaCategoriaRepository implements CategoriaRepository {

    private final CategoriaJpaRepository categoriaJpaRepository;
    private final Clock reloj;

    JpaCategoriaRepository(CategoriaJpaRepository categoriaJpaRepository, Clock reloj) {
        this.categoriaJpaRepository = categoriaJpaRepository;
        this.reloj = reloj;
    }

    @Override
    public List<Categoria> buscarActivas() {
        return categoriaJpaRepository.findByActivaTrue().stream()
                .map(JpaCategoriaRepository::aDominio)
                .toList();
    }

    @Override
    public List<Categoria> buscarTodas() {
        return categoriaJpaRepository.findAll().stream()
                .map(JpaCategoriaRepository::aDominio)
                .toList();
    }

    @Override
    public Optional<Categoria> buscarPorId(UUID categoriaId) {
        return categoriaJpaRepository.findById(categoriaId).map(JpaCategoriaRepository::aDominio);
    }

    @Override
    public void guardarActiva(Categoria categoria) {
        categoriaJpaRepository
                .findById(categoria.id())
                .orElseThrow()
                .cambiarActiva(categoria.activa(), reloj.instant());
    }

    private static Categoria aDominio(CategoriaEntity fila) {
        return new Categoria(fila.getId(), fila.getNombre(), fila.isActiva());
    }
}
