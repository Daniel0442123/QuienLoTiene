package com.quienlotiene.reputacion.domain.model;

/**
 * Calificación de una tienda (HU-034).
 *
 * @param promedio de 1 a 5 con un decimal; 0 si no tiene reseñas
 */
public record ResumenReputacion(double promedio, long totalResenas) {

    public static final ResumenReputacion SIN_RESENAS = new ResumenReputacion(0, 0);
}
