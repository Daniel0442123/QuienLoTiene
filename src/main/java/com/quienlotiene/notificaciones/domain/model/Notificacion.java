package com.quienlotiene.notificaciones.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Aviso en tiempo real para un usuario (HU-030, diagrama 20 paso 8): tipo y datos mínimos. El {@code id} permite
 * reenviar lo perdido cuando el cliente reconecta con {@code Last-Event-ID}.
 *
 * @param tipo nombre del evento SSE, por ejemplo {@code solicitud-nueva}
 * @param datos objeto que se envía como JSON; solo ids y datos mínimos, nunca datos personales de otro usuario
 */
public record Notificacion(String id, UUID usuarioId, String tipo, Object datos, Instant fecha) {}
