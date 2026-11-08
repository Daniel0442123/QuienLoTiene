package com.quienlotiene.tiendas.api;

import java.time.Instant;
import java.util.UUID;

/**
 * Se publica cuando el administrador aprueba o rechaza una tienda (HU-036, diagrama 30 paso 18). Lo escucha
 * notificaciones para avisar al dueño.
 *
 * @param estado {@code APROBADA} o {@code RECHAZADA}
 * @param motivoRechazo solo si fue rechazada
 */
public record TiendaRevisadaEvent(
        UUID tiendaId, UUID duenoId, String estado, String motivoRechazo, Instant fechaRevision) {}
