package com.quienlotiene.mensajeria.api;

import java.util.UUID;

/**
 * Llegó un mensaje al chat de una orden (HU-033). Notificaciones avisa a {@code destinatarioId}, la otra parte, sin el
 * texto: el frontend pide los mensajes por REST.
 */
public record MensajeEnviadoEvent(UUID ordenId, UUID mensajeId, UUID destinatarioId) {}
