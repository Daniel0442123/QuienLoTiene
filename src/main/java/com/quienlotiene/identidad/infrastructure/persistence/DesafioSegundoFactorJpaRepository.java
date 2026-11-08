package com.quienlotiene.identidad.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DesafioSegundoFactorJpaRepository extends JpaRepository<DesafioSegundoFactorEntity, UUID> {

    Optional<DesafioSegundoFactorEntity> findByHashToken(String hashToken);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update DesafioSegundoFactorEntity desafio
            set desafio.intentosFallidos = desafio.intentosFallidos + 1, desafio.actualizadoEn = :ahora
            where desafio.id = :desafioId
            """)
    int registrarIntentoFallido(@Param("desafioId") UUID desafioId, @Param("ahora") Instant ahora);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update DesafioSegundoFactorEntity desafio
            set desafio.usado = true, desafio.actualizadoEn = :ahora
            where desafio.id = :desafioId and desafio.usado = false
            """)
    int marcarUsadoSiNoLoEsta(@Param("desafioId") UUID desafioId, @Param("ahora") Instant ahora);
}
