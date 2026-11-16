package com.quienlotiene.solicitudes.api;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Lo que ordenes necesita de la respuesta elegida para crear la orden (HU-024).
 *
 * @param producto lo que se entrega: el producto pedido o, con PARECIDO, la descripción de la alternativa
 * @param cantidad unidades que ofreció la tienda
 */
public record RespuestaElegida(
        UUID solicitudId,
        UUID respuestaId,
        UUID tiendaId,
        UUID duenoTiendaId,
        String producto,
        int cantidad,
        long precioUnitarioEnPesos,
        @Nullable Long costoDomicilioCotizadoEnPesos) {}
