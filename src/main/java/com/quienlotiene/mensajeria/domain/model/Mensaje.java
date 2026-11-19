package com.quienlotiene.mensajeria.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** HU-033: un mensaje del chat de una orden. Solo lo escribe uno de sus dos participantes y no se edita. */
public record Mensaje(UUID id, UUID ordenId, UUID autorId, boolean autorEsCliente, String texto, Instant fecha) {

    public static final int LARGO_MAXIMO = 1000;

    public Mensaje {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ordenId, "ordenId");
        Objects.requireNonNull(autorId, "autorId");
        Objects.requireNonNull(fecha, "fecha");
    }

    /**
     * @throws MensajeInvalidoException si el texto está vacío o pasa de 1000 caracteres
     */
    public static Mensaje escribir(
            UUID id, UUID ordenId, UUID autorId, boolean autorEsCliente, String texto, Instant ahora) {
        var limpio = texto == null ? "" : texto.strip();
        if (limpio.isEmpty() || limpio.length() > LARGO_MAXIMO) {
            throw new MensajeInvalidoException();
        }
        return new Mensaje(id, ordenId, autorId, autorEsCliente, limpio, ahora);
    }
}
