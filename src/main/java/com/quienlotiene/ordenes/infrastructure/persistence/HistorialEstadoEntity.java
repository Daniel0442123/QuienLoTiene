package com.quienlotiene.ordenes.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de {@code ordenes.historial_estado}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "ordenes", name = "historial_estado")
public class HistorialEstadoEntity {

    @Id
    private UUID id;

    @Column(name = "orden_id", nullable = false, updatable = false)
    private UUID ordenId;

    @Column(nullable = false, length = 20, updatable = false)
    private String estado;

    @Column(name = "cambiado_en", nullable = false, updatable = false)
    private Instant cambiadoEn;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    HistorialEstadoEntity(UUID id, UUID ordenId, String estado, Instant cambiadoEn) {
        this.id = id;
        this.ordenId = ordenId;
        this.estado = estado;
        this.cambiadoEn = cambiadoEn;
        this.creadoEn = cambiadoEn;
        this.actualizadoEn = cambiadoEn;
    }
}
