package com.quienlotiene.identidad.infrastructure.persistence;

import com.quienlotiene.identidad.domain.model.DesafioSegundoFactor;
import com.quienlotiene.identidad.domain.port.DesafioSegundoFactorRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link DesafioSegundoFactorRepository}. */
@Component
class JpaDesafioSegundoFactorRepository implements DesafioSegundoFactorRepository {

    private final DesafioSegundoFactorJpaRepository desafioJpaRepository;
    private final EntityManager entityManager;

    JpaDesafioSegundoFactorRepository(
            DesafioSegundoFactorJpaRepository desafioJpaRepository, EntityManager entityManager) {
        this.desafioJpaRepository = desafioJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public void guardar(DesafioSegundoFactor desafio) {
        entityManager.persist(new DesafioSegundoFactorEntity(
                desafio.getId(),
                desafio.getUsuarioId(),
                desafio.getHashToken(),
                desafio.getFechaExpiracion(),
                desafio.getFechaExpiracion().minus(DesafioSegundoFactor.VIGENCIA)));
    }

    @Override
    public Optional<DesafioSegundoFactor> buscarPorHash(String hashToken) {
        return desafioJpaRepository
                .findByHashToken(hashToken)
                .map(fila -> DesafioSegundoFactor.reconstituir(
                        fila.getId(),
                        fila.getUsuarioId(),
                        fila.getHashToken(),
                        fila.getExpiraEn(),
                        fila.getIntentosFallidos(),
                        fila.isUsado()));
    }

    @Override
    public void registrarIntentoFallido(UUID desafioId, Instant ahora) {
        desafioJpaRepository.registrarIntentoFallido(desafioId, ahora);
    }

    @Override
    public boolean marcarUsadoSiNoLoEsta(UUID desafioId, Instant ahora) {
        return desafioJpaRepository.marcarUsadoSiNoLoEsta(desafioId, ahora) == 1;
    }
}
