package com.quienlotiene.solicitudes.api;

import java.util.List;
import java.util.UUID;

/**
 * Se publica cuando una solicitud llega a las tiendas elegibles (HU-017, diagrama 03 pasos 10 a 12). Lo escucha
 * notificaciones para avisar a cada tienda por SSE o push. No lleva datos del cliente.
 */
public record SolicitudDifundidaEvent(UUID solicitudId, int radioEnMetros, List<Destinatario> destinatarios) {

    public SolicitudDifundidaEvent {
        destinatarios = List.copyOf(destinatarios);
    }

    /** Tienda que recibió la solicitud y el usuario dueño al que hay que avisar. */
    public record Destinatario(UUID tiendaId, UUID duenoId, int distanciaEnMetros) {}
}
