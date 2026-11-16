package com.quienlotiene.solicitudes.domain.model;

/** Solicitud abierta que llegó a una tienda, con la distancia exacta a la que estaba el cliente (HU-018). */
public record SolicitudParaTienda(Solicitud solicitud, int distanciaEnMetros) {

    /** CP-SOL-018-02: lo que la tienda ve, redondeado a 100 m. */
    public int distanciaAproximadaEnMetros() {
        return DistanciaAproximada.redondear(distanciaEnMetros);
    }
}
