package com.quienlotiene.solicitudes.api;

import java.util.UUID;

/**
 * Una tienda respondió una solicitud (HU-019, diagrama 03 paso 16). Notificaciones avisa al cliente por SSE si la
 * respuesta es SI o PARECIDO (HU-020, CP-SOL-020-01).
 *
 * @param tipo {@code SI}, {@code PARECIDO} o {@code NO}
 */
public record RespuestaRecibidaEvent(
        UUID solicitudId, UUID respuestaId, UUID clienteId, String tipo, String nombreTienda) {}
