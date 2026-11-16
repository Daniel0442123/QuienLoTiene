package com.quienlotiene.solicitudes.api;

import java.util.List;
import java.util.UUID;

/**
 * Una solicitud venció sin elección (HU-021, CU-29, diagrama 33 paso 5). Notificaciones avisa al cliente y a las
 * tiendas que la recibieron.
 */
public record SolicitudVencidaEvent(
        UUID solicitudId, UUID clienteId, int respuestasRecibidas, List<UUID> duenosQueLaRecibieron) {

    public SolicitudVencidaEvent {
        duenosQueLaRecibieron = List.copyOf(duenosQueLaRecibieron);
    }
}
