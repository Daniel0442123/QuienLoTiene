package com.quienlotiene.reputacion.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de {@code reputacion.resena}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "reputacion", name = "resena")
public class ResenaEntity {

    @Id
    private UUID id;

    @Column(name = "orden_id", nullable = false, updatable = false)
    private UUID ordenId;

    @Column(name = "tienda_id", nullable = false, updatable = false)
    private UUID tiendaId;

    @Column(name = "cliente_id", nullable = false, updatable = false)
    private UUID clienteId;

    @Column(nullable = false, updatable = false)
    private short estrellas;

    @Column(length = 500, updatable = false)
    private String comentario;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    ResenaEntity(
            UUID id, UUID ordenId, UUID tiendaId, UUID clienteId, short estrellas, String comentario, Instant fecha) {
        this.id = id;
        this.ordenId = ordenId;
        this.tiendaId = tiendaId;
        this.clienteId = clienteId;
        this.estrellas = estrellas;
        this.comentario = comentario;
        this.creadoEn = fecha;
        this.actualizadoEn = fecha;
    }
}
