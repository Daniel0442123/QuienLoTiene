package com.quienlotiene.ordenes.api;

import java.util.UUID;

/** La orden cambió de estado (HU-027, HU-028, diagrama 04 paso 14). Notificaciones avisa al cliente y a la tienda. */
public record EstadoOrdenCambiadoEvent(UUID ordenId, UUID clienteId, UUID duenoTiendaId, String estado) {}
