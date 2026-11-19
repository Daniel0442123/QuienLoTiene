package com.quienlotiene.reputacion.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Calificación de 1 a 5 estrellas que deja el cliente sobre la tienda de una orden resuelta (glosario: reseña). Hay una
 * por orden (diagrama 09: {@code orden_id UNIQUE}).
 */
public record Resena(
        UUID id, UUID ordenId, UUID tiendaId, UUID clienteId, int estrellas, String comentario, Instant fecha) {

    public static final int MAXIMO_COMENTARIO = 500;

    public Resena {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ordenId, "ordenId");
        Objects.requireNonNull(tiendaId, "tiendaId");
        Objects.requireNonNull(clienteId, "clienteId");
        Objects.requireNonNull(fecha, "fecha");
        if (estrellas < 1 || estrellas > 5) {
            throw new CalificacionInvalidaException("califica de 1 a 5 estrellas");
        }
        if (comentario != null && comentario.isBlank()) {
            comentario = null;
        }
        if (comentario != null) {
            comentario = comentario.strip();
            if (comentario.length() > MAXIMO_COMENTARIO) {
                throw new CalificacionInvalidaException("el comentario puede tener hasta 500 caracteres");
            }
        }
    }
}
