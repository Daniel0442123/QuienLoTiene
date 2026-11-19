package com.quienlotiene.tiendas.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

/**
 * Detalle público de una tienda aprobada (HU-014, CU-06). No incluye el RUT, los documentos de verificación ni el motivo
 * de una revisión (CP-TIE-014-02). Las reseñas salen de reputacion en {@code GET /api/v1/tiendas/{id}/resenas}.
 */
@Schema(name = "TiendaPublicaResponse")
public record TiendaPublica(
        UUID id,
        String nombre,
        String telefono,
        UUID categoriaId,
        double latitud,
        double longitud,
        boolean estaAbierta,
        List<MiTienda.Franja> horario,
        Domicilio domicilio,

        @Schema(allowableValues = {"EFECTIVO", "TRANSFERENCIA", "DATAFONO"})
        List<String> mediosPago) {

    public TiendaPublica {
        horario = List.copyOf(horario);
        mediosPago = List.copyOf(mediosPago);
    }
}
