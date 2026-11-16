package com.quienlotiene.ordenes.domain.port;

import com.quienlotiene.ordenes.domain.model.Orden;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Órdenes y su historial de estados (esquema {@code ordenes}). */
public interface OrdenRepository {

    void guardarNueva(Orden orden);

    /** Guarda el estado nuevo y agrega al historial los cambios que todavía no estén guardados. */
    void guardarCambioEstado(Orden orden);

    Optional<Orden> buscarPorId(UUID ordenId);

    /** Órdenes donde el usuario es el cliente o el dueño de la tienda, de la más reciente a la más antigua. */
    List<Orden> listarDeUsuario(UUID usuarioId, int maximo);
}
