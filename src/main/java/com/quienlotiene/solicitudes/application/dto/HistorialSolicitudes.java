package com.quienlotiene.solicitudes.application.dto;

import java.util.List;

/**
 * Una página del historial (HU-008). Vacío en la primera página: la pantalla invita a publicar (CP-CLI-008-02).
 *
 * @param hayMas {@code true} si hay una página siguiente
 */
public record HistorialSolicitudes(List<SolicitudDelHistorial> solicitudes, int pagina, boolean hayMas) {

    public HistorialSolicitudes {
        solicitudes = List.copyOf(solicitudes);
    }
}
