package com.quienlotiene.administracion.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de {@code administracion.auditoria}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "administracion", name = "auditoria")
public class AuditoriaEntity {

    @Id
    private UUID id;

    @Column(name = "administrador_id", nullable = false, updatable = false)
    private UUID administradorId;

    @Column(nullable = false, length = 40, updatable = false)
    private String accion;

    @Column(name = "recurso_id", nullable = false, updatable = false)
    private UUID recursoId;

    @Column(length = 300, updatable = false)
    private String detalle;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false, updatable = false)
    private Instant actualizadoEn;

    public AuditoriaEntity(
            UUID id, UUID administradorId, String accion, UUID recursoId, String detalle, Instant creadoEn) {
        this.id = id;
        this.administradorId = administradorId;
        this.accion = accion;
        this.recursoId = recursoId;
        this.detalle = detalle;
        this.creadoEn = creadoEn;
        this.actualizadoEn = creadoEn;
    }
}
