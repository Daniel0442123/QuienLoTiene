package com.quienlotiene.solicitudes.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Se publica cuando una solicitud llega a las tiendas elegibles (HU-017, diagrama 03 pasos 10 a 12). Lo escucha
 * notificaciones para avisar a cada tienda por SSE (HU-030). Trae los datos de la tarjeta de HU-018 y ningún dato del
 * cliente (CP-SOL-018-01).
 */
public record SolicitudDifundidaEvent(
        UUID solicitudId,
        String producto,
        int cantidad,
        String modalidad,
        boolean esUrgente,
        Instant fechaVencimiento,
        int radioEnMetros,
        List<Destinatario> destinatarios) {

    public SolicitudDifundidaEvent {
        destinatarios = List.copyOf(destinatarios);
    }

    /**
     * Tienda que recibió la solicitud y el usuario dueño al que hay que avisar.
     *
     * @param distanciaAproximadaEnMetros redondeada a 100 m (CP-SOL-018-02)
     */
    public record Destinatario(UUID tiendaId, UUID duenoId, int distanciaAproximadaEnMetros) {}
}
