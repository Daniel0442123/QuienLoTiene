package com.quienlotiene.ordenes.api;

import java.util.UUID;

/** Fachada para que mensajeria sepa si la orden está activa y con quién comunicar al usuario (HU-032, diagrama 18). */
public interface OrdenesApi {

    /**
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException ORD-001 si la orden no existe o el usuario no es
     *     ni su cliente ni su tienda (R-03)
     */
    ContactoDeOrden obtenerContactoDeLaOtraParte(UUID ordenId, UUID usuarioId);
}
