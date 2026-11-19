package com.quienlotiene.notificaciones.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de {@code notificaciones.suscripcion_push}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "notificaciones", name = "suscripcion_push")
public class SuscripcionPushEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(nullable = false, length = 1000, updatable = false)
    private String endpoint;

    @Column(nullable = false, length = 100, updatable = false)
    private String p256dh;

    @Column(nullable = false, length = 50, updatable = false)
    private String auth;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    SuscripcionPushEntity(UUID id, UUID usuarioId, String endpoint, String p256dh, String auth, Instant creadoEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.endpoint = endpoint;
        this.p256dh = p256dh;
        this.auth = auth;
        this.creadoEn = creadoEn;
    }
}
