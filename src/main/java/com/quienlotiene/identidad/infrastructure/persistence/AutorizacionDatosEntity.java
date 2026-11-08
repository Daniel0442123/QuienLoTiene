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

/** Fila de {@code identidad.autorizacion_datos}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "identidad", name = "autorizacion_datos")
public class AutorizacionDatosEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "version_politica", nullable = false, length = 10, updatable = false)
    private String versionPolitica;

    @Column(name = "aceptada_en", nullable = false, updatable = false)
    private Instant aceptadaEn;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public AutorizacionDatosEntity(
            UUID id,
            UUID usuarioId,
            String versionPolitica,
            Instant aceptadaEn,
            Instant creadoEn,
            Instant actualizadoEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.versionPolitica = versionPolitica;
        this.aceptadaEn = aceptadaEn;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }
}
