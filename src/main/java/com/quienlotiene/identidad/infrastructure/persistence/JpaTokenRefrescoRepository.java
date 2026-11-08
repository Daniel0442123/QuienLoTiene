package com.quienlotiene.identidad.infrastructure.persistence;

import com.quienlotiene.identidad.domain.model.TokenRefresco;
import com.quienlotiene.identidad.domain.port.TokenRefrescoRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link TokenRefrescoRepository}. */
@Component
class JpaTokenRefrescoRepository implements TokenRefrescoRepository {

    private final TokenRefrescoJpaRepository tokenJpaRepository;
    private final EntityManager entityManager;

    JpaTokenRefrescoRepository(TokenRefrescoJpaRepository tokenJpaRepository, EntityManager entityManager) {
        this.tokenJpaRepository = tokenJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public void guardar(TokenRefresco token) {
        entityManager.persist(new TokenRefrescoEntity(
                token.getId(),
                token.getUsuarioId(),
                token.getHashToken(),
                token.getFechaExpiracion(),
                token.estaRevocado(),
                token.getFechaCreacion(),
                token.getFechaCreacion()));
    }

    @Override
    public Optional<TokenRefresco> buscarPorHash(String hashToken) {
        return tokenJpaRepository
                .findByHashToken(hashToken)
                .map(fila -> TokenRefresco.reconstituir(
                        fila.getId(),
                        fila.getUsuarioId(),
                        fila.getHashToken(),
                        fila.getExpiraEn(),
                        fila.isRevocado(),
                        fila.getCreadoEn()));
    }

    @Override
    public boolean revocarSiSigueActivo(UUID tokenId, Instant ahora) {
        return tokenJpaRepository.revocarSiSigueActivo(tokenId, ahora) == 1;
    }

    @Override
    public void revocarTodosDe(UUID usuarioId, Instant ahora) {
        tokenJpaRepository.revocarTodosDe(usuarioId, ahora);
    }
}
