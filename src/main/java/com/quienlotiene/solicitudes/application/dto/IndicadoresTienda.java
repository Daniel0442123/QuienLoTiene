package com.quienlotiene.solicitudes.application.dto;

import org.jspecify.annotations.Nullable;

/**
 * HU-015 «Tu resumen» (mockup 06, SC-011 punto 6).
 *
 * @param periodoEnDias 7 o 30
 * @param porcentajeRespondiste {@code null} si no le llegó ninguna solicitud en el periodo
 * @param porcentajeTeEligieron {@code null} si no respondió «Sí lo tengo» ni «Tengo algo parecido» en el periodo
 */
public record IndicadoresTienda(
        int periodoEnDias,
        int recibidas,
        int respondidas,
        int respuestasConProducto,
        int ganadas,
        @Nullable Integer porcentajeRespondiste,
        @Nullable Integer porcentajeTeEligieron) {}
