package com.quienlotiene.solicitudes.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Regla de HU-016: un cliente publica como máximo {@code maximoPorHora} solicitudes en cualquier ventana de una hora.
 * Cuando llega al tope, puede volver a publicar en cuanto la más antigua de esa hora cumpla los 60 minutos.
 */
public final class LimitePublicacion {

    public static final Duration VENTANA = Duration.ofHours(1);

    private LimitePublicacion() {}

    /** Desde cuándo hay que contar las publicaciones del cliente. */
    public static Instant inicioDeVentana(Instant ahora) {
        return ahora.minus(VENTANA);
    }

    /**
     * @param fechasPublicacionEnVentana fechas de las solicitudes del cliente publicadas desde {@link #inicioDeVentana}
     * @throws LimiteSolicitudesSuperadoException si ya publicó {@code maximoPorHora} o más
     */
    public static void verificar(List<Instant> fechasPublicacionEnVentana, int maximoPorHora, Instant ahora) {
        if (fechasPublicacionEnVentana.size() < maximoPorHora) {
            return;
        }
        // La que libera el cupo es la que hace que queden maximoPorHora - 1 en la ventana
        var fechasDeMasNuevaAMasVieja = fechasPublicacionEnVentana.stream()
                .sorted(Comparator.reverseOrder())
                .toList();
        var fechaQueLiberaCupo = fechasDeMasNuevaAMasVieja.get(maximoPorHora - 1);
        var tiempoDeEspera = Duration.between(ahora, fechaQueLiberaCupo.plus(VENTANA));
        throw new LimiteSolicitudesSuperadoException(maximoPorHora, tiempoDeEspera);
    }
}
