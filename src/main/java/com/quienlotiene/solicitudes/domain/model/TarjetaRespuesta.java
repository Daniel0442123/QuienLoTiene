package com.quienlotiene.solicitudes.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Lo que el cliente ve de una respuesta SI o PARECIDO (HU-020): precio, unidades, domicilio, total y distancia.
 *
 * @param costoDomicilioEnPesos {@code null} si la tienda no lleva domicilio a la ubicación de la solicitud
 */
public record TarjetaRespuesta(
        UUID respuestaId,
        UUID tiendaId,
        TipoRespuesta tipo,
        String descripcionAlternativa,
        long precioEnPesos,
        int cantidadDisponible,
        long subtotalEnPesos,
        Long costoDomicilioEnPesos,
        long totalEnPesos,
        int distanciaEnMetros,
        Integer tiempoEstimadoEnMinutos,
        Instant fechaRespuesta,
        boolean fueElegida) {

    /** CP-SOL-020-03: «Tengo algo parecido» se marca como alternativa. */
    public boolean esAlternativa() {
        return tipo == TipoRespuesta.PARECIDO;
    }

    public boolean ofreceDomicilio() {
        return costoDomicilioEnPesos != null;
    }
}
