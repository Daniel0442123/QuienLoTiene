package com.quienlotiene.shared.domain;

/**
 * Punto en el mapa en grados decimales WGS 84 (SRID 4326), como lo entrega el GPS del teléfono. Lo usan solicitudes,
 * tiendas y direcciones del cliente.
 */
public record Ubicacion(double latitud, double longitud) {

    public static final double LATITUD_MAXIMA = 90;
    public static final double LONGITUD_MAXIMA = 180;

    /** Radio medio de la Tierra (IUGG), suficiente para distancias de pocos kilómetros. */
    private static final double RADIO_TIERRA_METROS = 6_371_008.8;

    public Ubicacion {
        if (Double.isNaN(latitud) || Math.abs(latitud) > LATITUD_MAXIMA) {
            throw new UbicacionInvalidaException();
        }
        if (Double.isNaN(longitud) || Math.abs(longitud) > LONGITUD_MAXIMA) {
            throw new UbicacionInvalidaException();
        }
    }

    /** Distancia en línea recta sobre la esfera (fórmula del haversine), en metros. */
    public double distanciaEnMetrosA(Ubicacion otra) {
        var latitudOrigen = Math.toRadians(latitud);
        var latitudDestino = Math.toRadians(otra.latitud);
        var diferenciaLatitud = latitudDestino - latitudOrigen;
        var diferenciaLongitud = Math.toRadians(otra.longitud - longitud);
        var haversine = Math.pow(Math.sin(diferenciaLatitud / 2), 2)
                + Math.cos(latitudOrigen) * Math.cos(latitudDestino) * Math.pow(Math.sin(diferenciaLongitud / 2), 2);
        return 2 * RADIO_TIERRA_METROS * Math.asin(Math.min(1, Math.sqrt(haversine)));
    }
}
