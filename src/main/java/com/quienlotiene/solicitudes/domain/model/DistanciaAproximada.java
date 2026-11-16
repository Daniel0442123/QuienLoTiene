package com.quienlotiene.solicitudes.domain.model;

/**
 * CP-SOL-018-02: la tienda ve la distancia al cliente redondeada a 100 m (mínimo 100 m), para no revelar su ubicación
 * exacta. El frontend la muestra como «a 1,2 km».
 */
public final class DistanciaAproximada {

    public static final int PASO_METROS = 100;

    private DistanciaAproximada() {}

    public static int redondear(int distanciaEnMetros) {
        var redondeada = Math.round(distanciaEnMetros / (double) PASO_METROS) * PASO_METROS;
        return (int) Math.max(PASO_METROS, redondeada);
    }
}
