package com.quienlotiene.catalogo.infrastructure.persistence;

import com.quienlotiene.catalogo.domain.model.PalabraRestringida;
import com.quienlotiene.catalogo.domain.port.PalabraRestringidaRepository;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link PalabraRestringidaRepository}. La lista es corta: se lee completa. */
@Component
class JpaPalabraRestringidaRepository implements PalabraRestringidaRepository {

    private final ProductoRestringidoJpaRepository jpaRepository;
    private final Clock reloj;

    JpaPalabraRestringidaRepository(ProductoRestringidoJpaRepository jpaRepository, Clock reloj) {
        this.jpaRepository = jpaRepository;
        this.reloj = reloj;
    }

    @Override
    public List<PalabraRestringida> listar() {
        return jpaRepository.findAll().stream()
                .map(JpaPalabraRestringidaRepository::aDominio)
                .toList();
    }

    @Override
    public boolean existe(String palabraNormalizada) {
        return jpaRepository.existsByPalabraClave(palabraNormalizada);
    }

    @Override
    public Optional<PalabraRestringida> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(JpaPalabraRestringidaRepository::aDominio);
    }

    @Override
    public void guardar(PalabraRestringida palabra) {
        jpaRepository.save(new ProductoRestringidoEntity(palabra.id(), palabra.palabraClave(), reloj.instant()));
    }

    @Override
    public void eliminar(UUID id) {
        jpaRepository.deleteById(id);
    }

    private static PalabraRestringida aDominio(ProductoRestringidoEntity fila) {
        return new PalabraRestringida(fila.getId(), fila.getPalabraClave());
    }
}
