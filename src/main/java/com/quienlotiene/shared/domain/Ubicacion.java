package com.quienlotiene.shared.domain;

/**
 * Punto en el mapa en grados decimales WGS 84 (SRID 4326), como lo entrega el GPS del teléfono. Lo usan solicitudes,
 * tiendas y direcciones del cliente.
 */
public record Ubicacion(double latitud, double longitud) {

    public static final double LATITUD_MAXIMA = 90;
    public static final double LONGITUD_MAXIMA = 180;

    public Ubicacion {
        if (Double.isNaN(latitud) || Math.abs(latitud) > LATITUD_MAXIMA) {
            throw new UbicacionInvalidaException();
        }
        if (Double.isNaN(longitud) || Math.abs(longitud) > LONGITUD_MAXIMA) {
            throw new UbicacionInvalidaException();
        }
    }
}
