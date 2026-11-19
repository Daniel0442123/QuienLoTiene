package com.quienlotiene.ordenes.api;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * La orden cambió de estado (HU-025, HU-027, HU-028, HU-029; diagrama 04 paso 14). Notificaciones avisa al cliente y a
 * la tienda.
 *
 * @param motivo el motivo de la cancelación cuando el estado es CANCELADO; {@code null} en los demás
 */
public record EstadoOrdenCambiadoEvent(
        UUID ordenId,
        UUID clienteId,
        UUID duenoTiendaId,
        String estado,
        @Nullable String motivo) {

    public EstadoOrdenCambiadoEvent(UUID ordenId, UUID clienteId, UUID duenoTiendaId, String estado) {
        this(ordenId, clienteId, duenoTiendaId, estado, null);
    }
}
