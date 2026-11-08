package com.quienlotiene.tiendas.application.dto;

/**
 * Configuración de domicilio de la tienda, tal como la ve y la envía su dueño.
 *
 * @param tipoTarifa {@code FIJA} (HU-010); {@code null} si no hace domicilio
 * @param tarifaFijaEnPesos valor de la tarifa fija; {@code null} si no hace domicilio
 */
public record Domicilio(
        boolean haceDomicilio, Integer radioCoberturaEnMetros, String tipoTarifa, Long tarifaFijaEnPesos) {}
