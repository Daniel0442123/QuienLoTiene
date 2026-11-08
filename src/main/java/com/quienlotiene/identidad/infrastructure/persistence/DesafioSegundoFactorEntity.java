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

/** Fila de {@code identidad.desafio_segundo_factor}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "identidad", name = "desafio_segundo_factor")
public class DesafioSegundoFactorEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "hash_token", nullable = false, length = 64, updatable = false)
    private String hashToken;

    @Column(name = "expira_en", nullable = false, updatable = false)
    private Instant expiraEn;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(nullable = false)
    private boolean usado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public DesafioSegundoFactorEntity(UUID id, UUID usuarioId, String hashToken, Instant expiraEn, Instant creadoEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.hashToken = hashToken;
        this.expiraEn = expiraEn;
        this.intentosFallidos = 0;
        this.usado = false;
        this.creadoEn = creadoEn;
        this.actualizadoEn = creadoEn;
    }
}
