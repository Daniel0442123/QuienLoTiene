package com.quienlotiene.tiendas.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Consulta de tiendas para otros módulos (diagrama 07: "api: tiendas elegibles"). La usa solicitudes para difundir una
 * solicitud nueva (HU-017, diagrama 03 pasos 5 y 6).
 */
public interface TiendasApi {

    /**
     * Tiendas que pueden recibir una solicitud: APROBADAS, abiertas, dentro de su horario en {@code momento}, de la
     * categoría y a {@code radioEnMetros} o menos del cliente. Ordenadas de la más cercana a la más lejana.
     */
    List<TiendaElegible> buscarElegibles(
            UUID categoriaId, double latitud, double longitud, int radioEnMetros, Instant momento);
}
