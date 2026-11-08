package com.quienlotiene.solicitudes.domain.port;

import com.quienlotiene.solicitudes.domain.model.Solicitud;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Guarda y consulta las solicitudes del esquema {@code solicitudes}. */
public interface SolicitudRepository {

    void guardarNueva(Solicitud solicitud);

    /** Fechas de publicación de las solicitudes del cliente desde {@code desde} (para el límite por hora). */
    List<Instant> listarFechasPublicacionDesde(UUID clienteId, Instant desde);
}
