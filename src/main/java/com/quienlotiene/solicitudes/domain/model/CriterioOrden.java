package com.quienlotiene.solicitudes.domain.model;

import java.util.Comparator;

/** Cómo ordena el cliente las tarjetas de respuesta (HU-020, CP-SOL-020-02). Empates: la que llegó primero. */
public enum CriterioOrden {
    /** Precio por unidad, del más barato al más caro. */
    PRECIO(Comparator.comparingLong(TarjetaRespuesta::precioEnPesos)),
    /** Distancia de la tienda, de la más cercana a la más lejana. */
    DISTANCIA(Comparator.comparingInt(TarjetaRespuesta::distanciaEnMetros)),
    /** Total con domicilio, del más barato al más caro. */
    TOTAL(Comparator.comparingLong(TarjetaRespuesta::totalEnPesos));

    private final Comparator<TarjetaRespuesta> comparador;

    CriterioOrden(Comparator<TarjetaRespuesta> comparador) {
        this.comparador = comparador.thenComparing(TarjetaRespuesta::fechaRespuesta);
    }

    public Comparator<TarjetaRespuesta> comparador() {
        return comparador;
    }

    /** {@code precio}, {@code distancia} o {@code total}; cualquier otro valor ordena por precio. */
    public static CriterioOrden desde(String valor) {
        if (valor == null) {
            return PRECIO;
        }
        return switch (valor.strip().toLowerCase(java.util.Locale.ROOT)) {
            case "distancia" -> DISTANCIA;
            case "total" -> TOTAL;
            default -> PRECIO;
        };
    }
}
