package com.quienlotiene.notificaciones.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo de {@code POST /api/v1/notificaciones/suscripciones}: el {@code PushSubscription.toJSON()} del navegador. */
record SuscribirPushRequest(
        @NotBlank @Size(max = 1000) String endpoint,
        @NotNull @Valid Claves keys) {

    record Claves(
            @NotBlank @Size(max = 100) String p256dh,
            @NotBlank @Size(max = 50) String auth) {}
}
