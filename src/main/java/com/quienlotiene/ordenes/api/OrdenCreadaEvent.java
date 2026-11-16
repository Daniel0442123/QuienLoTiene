package com.quienlotiene.ordenes.api;

import java.util.UUID;

/** Se creó una orden (HU-024, diagrama 04 paso 8). Notificaciones avisa a la tienda elegida. */
public record OrdenCreadaEvent(
        UUID ordenId,
        UUID solicitudId,
        UUID clienteId,
        UUID duenoTiendaId,
        String producto,
        long totalEnPesos,
        String modalidad) {}
