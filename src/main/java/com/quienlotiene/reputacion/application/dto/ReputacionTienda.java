package com.quienlotiene.reputacion.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Calificación de una tienda para su detalle (HU-014, HU-034).
 *
 * @param promedio de 1 a 5 con un decimal; 0 si no tiene reseñas
 * @param recientes las 10 más recientes, sin datos del cliente
 */
@Schema(name = "ReputacionTiendaResponse")
public record ReputacionTienda(UUID tiendaId, double promedio, long totalResenas, List<ResenaPublica> recientes) {

    public ReputacionTienda {
        recientes = List.copyOf(recientes);
    }

    @Schema(name = "ResenaPublica")
    public record ResenaPublica(int estrellas, @Nullable String comentario, Instant fecha) {}
}
