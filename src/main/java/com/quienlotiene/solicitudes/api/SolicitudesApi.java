package com.quienlotiene.solicitudes.api;

import java.util.UUID;

/**
 * Fachada para que ordenes cree la orden al elegir una respuesta (HU-024, diagrama 04 paso 3). Se decidió fachada y
 * no evento (decisión 3 del backlog del Sprint 2): corre en la transacción de ordenes, así que si la orden no se puede
 * crear la solicitud tampoco queda RESUELTA.
 */
public interface SolicitudesApi {

    /**
     * Marca la respuesta como elegida con el id de la orden, deja la solicitud RESUELTA y publica
     * {@link RespuestaElegidaEvent}.
     *
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException SOL-007 si la respuesta no existe o no es de una
     *     solicitud del cliente, SOL-004 si la solicitud ya no está abierta o venció, SOL-008 si la respuesta es NO
     */
    RespuestaElegida elegirRespuesta(UUID clienteId, UUID respuestaId, UUID ordenId);
}
