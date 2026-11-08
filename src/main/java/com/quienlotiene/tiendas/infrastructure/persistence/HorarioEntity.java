package com.quienlotiene.tiendas.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de {@code tiendas.horario}: una franja de un día de una tienda. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "tiendas", name = "horario")
public class HorarioEntity {

    @Id
    private UUID id;

    @Column(name = "tienda_id", nullable = false, updatable = false)
    private UUID tiendaId;

    /** ISO 8601: 1 = lunes ... 7 = domingo. */
    @Column(name = "dia_semana", nullable = false)
    private short diaSemana;

    @Column(nullable = false)
    private LocalTime abre;

    @Column(nullable = false)
    private LocalTime cierra;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public HorarioEntity(UUID id, UUID tiendaId, short diaSemana, LocalTime abre, LocalTime cierra, Instant creadoEn) {
        this.id = id;
        this.tiendaId = tiendaId;
        this.diaSemana = diaSemana;
        this.abre = abre;
        this.cierra = cierra;
        this.creadoEn = creadoEn;
        this.actualizadoEn = creadoEn;
    }
}
