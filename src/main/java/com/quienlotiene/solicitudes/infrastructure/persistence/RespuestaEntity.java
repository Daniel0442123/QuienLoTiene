package com.quienlotiene.solicitudes.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de {@code solicitudes.respuesta}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "solicitudes", name = "respuesta")
public class RespuestaEntity {

    @Id
    private UUID id;

    @Column(name = "solicitud_id", nullable = false, updatable = false)
    private UUID solicitudId;

    @Column(name = "tienda_id", nullable = false, updatable = false)
    private UUID tiendaId;

    @Column(name = "dueno_id", nullable = false, updatable = false)
    private UUID duenoId;

    @Column(nullable = false, length = 10, updatable = false)
    private String tipo;

    @Column(name = "precio_pesos", updatable = false)
    private Long precioPesos;

    @Column(name = "cantidad_disponible", updatable = false)
    private Integer cantidadDisponible;

    @Column(name = "descripcion_alternativa", length = 300, updatable = false)
    private String descripcionAlternativa;

    @Column(name = "tiempo_estimado_minutos", updatable = false)
    private Integer tiempoEstimadoMinutos;

    @Column(name = "costo_domicilio_pesos", updatable = false)
    private Long costoDomicilioPesos;

    @Column(name = "orden_id")
    private UUID ordenId;

    @Version
    private long version;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    RespuestaEntity(
            UUID id,
            UUID solicitudId,
            UUID tiendaId,
            UUID duenoId,
            String tipo,
            Long precioPesos,
            Integer cantidadDisponible,
            String descripcionAlternativa,
            Integer tiempoEstimadoMinutos,
            Long costoDomicilioPesos,
            Instant creadoEn) {
        this.id = id;
        this.solicitudId = solicitudId;
        this.tiendaId = tiendaId;
        this.duenoId = duenoId;
        this.tipo = tipo;
        this.precioPesos = precioPesos;
        this.cantidadDisponible = cantidadDisponible;
        this.descripcionAlternativa = descripcionAlternativa;
        this.tiempoEstimadoMinutos = tiempoEstimadoMinutos;
        this.costoDomicilioPesos = costoDomicilioPesos;
        this.creadoEn = creadoEn;
        this.actualizadoEn = creadoEn;
    }

    void elegir(UUID ordenId, Instant ahora) {
        this.ordenId = ordenId;
        this.actualizadoEn = ahora;
    }
}
