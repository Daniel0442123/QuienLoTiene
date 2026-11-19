package com.quienlotiene.ordenes.api;

import java.util.UUID;

/**
 * Fachada de ordenes para otros módulos: mensajeria (HU-032, HU-033, diagrama 18) y reputacion (HU-034, HU-035).
 */
public interface OrdenesApi {

    /**
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException ORD-001 si la orden no existe o el usuario no es
     *     ni su cliente ni su tienda (R-03)
     */
    ContactoDeOrden obtenerContactoDeLaOtraParte(UUID ordenId, UUID usuarioId);

    /**
     * HU-033, HU-034 y HU-035: estado y participantes de la orden.
     *
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException ORD-001 si la orden no existe o el usuario no es
     *     ni su cliente ni su tienda (R-03)
     */
    ResumenDeOrden obtenerResumen(UUID ordenId, UUID usuarioId);
}
