package com.quienlotiene.solicitudes.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de {@code solicitudes.envio_solicitud}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "solicitudes", name = "envio_solicitud")
public class EnvioSolicitudEntity {

    @Id
    private UUID id;

    @Column(name = "solicitud_id", nullable = false, updatable = false)
    private UUID solicitudId;

    @Column(name = "tienda_id", nullable = false, updatable = false)
    private UUID tiendaId;

    @Column(name = "distancia_metros", nullable = false, updatable = false)
    private int distanciaMetros;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public EnvioSolicitudEntity(UUID id, UUID solicitudId, UUID tiendaId, int distanciaMetros, Instant creadoEn) {
        this.id = id;
        this.solicitudId = solicitudId;
        this.tiendaId = tiendaId;
        this.distanciaMetros = distanciaMetros;
        this.creadoEn = creadoEn;
        this.actualizadoEn = creadoEn;
    }
}
