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

/** Fila de {@code reputacion.reporte}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "reputacion", name = "reporte")
public class ReporteEntity {

    @Id
    private UUID id;

    @Column(name = "orden_id", nullable = false, updatable = false)
    private UUID ordenId;

    @Column(name = "tienda_id", nullable = false, updatable = false)
    private UUID tiendaId;

    @Column(name = "autor_id", nullable = false, updatable = false)
    private UUID autorId;

    @Column(name = "autor_es_cliente", nullable = false, updatable = false)
    private boolean autorEsCliente;

    @Column(nullable = false, length = 30, updatable = false)
    private String motivo;

    @Column(length = 1000, updatable = false)
    private String descripcion;

    @Column(nullable = false, length = 10)
    private String estado;

    @Column(name = "nota_resolucion", length = 500)
    private String notaResolucion;

    @Column(name = "resuelto_en")
    private Instant resueltoEn;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    ReporteEntity(
            UUID id,
            UUID ordenId,
            UUID tiendaId,
            UUID autorId,
            boolean autorEsCliente,
            String motivo,
            String descripcion,
            String estado,
            Instant creadoEn) {
        this.id = id;
        this.ordenId = ordenId;
        this.tiendaId = tiendaId;
        this.autorId = autorId;
        this.autorEsCliente = autorEsCliente;
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.actualizadoEn = creadoEn;
    }

    void resolver(String nuevoEstado, String nota, Instant fecha) {
        this.estado = nuevoEstado;
        this.notaResolucion = nota;
        this.resueltoEn = fecha;
        this.actualizadoEn = fecha;
    }
}
