package com.quienlotiene.identidad.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TokenRefrescoJpaRepository extends JpaRepository<TokenRefrescoEntity, UUID> {

    Optional<TokenRefrescoEntity> findByHashToken(String hashToken);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update TokenRefrescoEntity token
            set token.revocado = true, token.actualizadoEn = :ahora
            where token.id = :tokenId and token.revocado = false
            """)
    int revocarSiSigueActivo(@Param("tokenId") UUID tokenId, @Param("ahora") Instant ahora);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update TokenRefrescoEntity token
            set token.revocado = true, token.actualizadoEn = :ahora
            where token.usuarioId = :usuarioId and token.revocado = false
            """)
    int revocarTodosDe(@Param("usuarioId") UUID usuarioId, @Param("ahora") Instant ahora);
}
