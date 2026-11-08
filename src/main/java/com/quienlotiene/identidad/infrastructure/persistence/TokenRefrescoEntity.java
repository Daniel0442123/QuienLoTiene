package com.quienlotiene.identidad.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de {@code identidad.token_refresco}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "identidad", name = "token_refresco")
public class TokenRefrescoEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "hash_token", nullable = false, length = 64, updatable = false)
    private String hashToken;

    @Column(name = "expira_en", nullable = false, updatable = false)
    private Instant expiraEn;

    @Column(nullable = false)
    private boolean revocado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public TokenRefrescoEntity(
            UUID id,
            UUID usuarioId,
            String hashToken,
            Instant expiraEn,
            boolean revocado,
            Instant creadoEn,
            Instant actualizadoEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.hashToken = hashToken;
        this.expiraEn = expiraEn;
        this.revocado = revocado;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }
}
