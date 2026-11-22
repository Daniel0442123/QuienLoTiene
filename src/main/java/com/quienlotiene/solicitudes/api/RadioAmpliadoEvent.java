package com.quienlotiene.solicitudes.api;

import java.util.UUID;

/**
 * HU-022 (CU-31, diagrama 35 pasos 7 y 8): nadie respondió a tiempo y la solicitud se envió con un radio mayor.
 * Notificaciones le avisa al cliente; a las tiendas nuevas ya les llega la solicitud con {@link SolicitudDifundidaEvent}.
 *
 * @param tiendasNuevas cuántas tiendas la recibieron por primera vez con el radio nuevo
 */
public record RadioAmpliadoEvent(UUID solicitudId, UUID clienteId, int radioEnMetros, int tiendasNuevas) {}
