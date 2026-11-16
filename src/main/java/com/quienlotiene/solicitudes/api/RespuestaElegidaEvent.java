package com.quienlotiene.solicitudes.api;

import java.util.List;
import java.util.UUID;

/**
 * El cliente eligió una respuesta y la solicitud quedó RESUELTA (HU-024, diagrama 05). Notificaciones avisa a las
 * tiendas que la recibieron que se cerró y a las que respondieron sin ser elegidas (CP-SOL-021-02).
 *
 * @param duenosQueLaRecibieron dueños de todas las tiendas a las que llegó la solicitud
 * @param noElegidas respuestas SI o PARECIDO de otras tiendas
 */
public record RespuestaElegidaEvent(
        UUID solicitudId,
        UUID respuestaId,
        UUID ordenId,
        UUID clienteId,
        List<UUID> duenosQueLaRecibieron,
        List<RespuestaNoElegida> noElegidas) {

    public RespuestaElegidaEvent {
        duenosQueLaRecibieron = List.copyOf(duenosQueLaRecibieron);
        noElegidas = List.copyOf(noElegidas);
    }

    public record RespuestaNoElegida(UUID respuestaId, UUID duenoId) {}
}
