package com.quienlotiene.notificaciones.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Últimos avisos de un usuario, para reenviar los que perdió mientras estaba desconectado (diagrama 20 pasos 11 a 13).
 * Guarda los de los últimos {@link #RETENCION} y como máximo {@link #MAXIMO_AVISOS}.
 */
public final class AvisosRecientes {

    public static final Duration RETENCION = Duration.ofMinutes(5);
    public static final int MAXIMO_AVISOS = 100;

    private final Deque<Notificacion> avisos = new ArrayDeque<>();

    public synchronized void agregar(Notificacion notificacion) {
        avisos.addLast(notificacion);
        while (avisos.size() > MAXIMO_AVISOS) {
            avisos.removeFirst();
        }
    }

    /**
     * Los avisos posteriores a {@code ultimoIdRecibido} que todavía no superan la retención. Si el id no se encuentra
     * (muy viejo o de antes de reiniciar la API) se reenvían todos los retenidos: es mejor repetir que perder.
     */
    public synchronized List<Notificacion> posterioresA(String ultimoIdRecibido, Instant ahora) {
        descartarViejos(ahora);
        if (ultimoIdRecibido == null || ultimoIdRecibido.isBlank()) {
            return List.of();
        }
        var lista = List.copyOf(avisos);
        for (var posicion = 0; posicion < lista.size(); posicion++) {
            if (lista.get(posicion).id().equals(ultimoIdRecibido)) {
                return lista.subList(posicion + 1, lista.size());
            }
        }
        return lista;
    }

    public synchronized boolean estaVacio(Instant ahora) {
        descartarViejos(ahora);
        return avisos.isEmpty();
    }

    private void descartarViejos(Instant ahora) {
        var limite = ahora.minus(RETENCION);
        while (!avisos.isEmpty() && avisos.peekFirst().fecha().isBefore(limite)) {
            avisos.removeFirst();
        }
    }
}
