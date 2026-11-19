package com.quienlotiene.ordenes.api;

import java.util.UUID;

/**
 * Lo que otros módulos necesitan saber de una orden para reseñas, reportes y chat (HU-033, HU-034, HU-035), sin datos
 * personales.
 *
 * @param estado uno de los estados de la orden (CONFIRMADO ... VENCIDA)
 */
public record ResumenDeOrden(
        UUID ordenId,
        UUID clienteId,
        UUID tiendaId,
        UUID duenoTiendaId,
        String nombreTienda,
        String producto,
        String estado,
        boolean esActiva) {

    public boolean esDelCliente(UUID usuarioId) {
        return clienteId.equals(usuarioId);
    }

    /** El otro participante de la orden: la tienda si escribe el cliente, el cliente si escribe la tienda. */
    public UUID otraParte(UUID usuarioId) {
        return esDelCliente(usuarioId) ? duenoTiendaId : clienteId;
    }
}
