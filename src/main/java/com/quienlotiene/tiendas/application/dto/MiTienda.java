package com.quienlotiene.tiendas.application.dto;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/** Lo que el dueño ve de su propia tienda (CU-16 pasos 2 a 5). No incluye los documentos de verificación. */
public record MiTienda(
        UUID id,
        String nombre,
        String telefono,
        UUID categoriaId,
        double latitud,
        double longitud,
        String estado,
        boolean estaAbierta,
        String motivoRechazo,
        Instant fechaRegistro,
        List<Franja> horario) {

    public MiTienda {
        horario = List.copyOf(horario);
    }

    /** @param diaSemana {@code MONDAY} a {@code SUNDAY} */
    public record Franja(String diaSemana, LocalTime abre, LocalTime cierra) {}
}
