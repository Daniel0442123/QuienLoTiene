package com.quienlotiene.tiendas.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Nombre y estado de una tienda para mostrarla junto a otros datos, como un reporte (HU-037). */
public record TiendaResumen(
        UUID id,
        String nombre,

        @Schema(allowableValues = {"PENDIENTE", "APROBADA", "RECHAZADA", "SUSPENDIDA"})
        String estado) {}
