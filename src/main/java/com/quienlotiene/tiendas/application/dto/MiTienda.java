package com.quienlotiene.tiendas.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Lo que el dueño ve de su propia tienda (CU-16 pasos 2 a 5). No incluye los documentos de verificación. */
public record MiTienda(
        UUID id,
        String nombre,
        String telefono,
        UUID categoriaId,
        double latitud,
        double longitud,

        @Schema(allowableValues = {"PENDIENTE", "APROBADA", "RECHAZADA", "SUSPENDIDA"})
        String estado,

        boolean estaAbierta,
        @Nullable String motivoRechazo,
        Instant fechaRegistro,
        List<Franja> horario) {

    public MiTienda {
        horario = List.copyOf(horario);
    }

    /** @param diaSemana {@code MONDAY} a {@code SUNDAY} */
    public record Franja(
            @Schema(allowableValues = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"})
            String diaSemana,

            LocalTime abre,
            LocalTime cierra) {}
}
