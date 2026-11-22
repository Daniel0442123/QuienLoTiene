package com.quienlotiene.solicitudes.domain.port;

import com.quienlotiene.solicitudes.domain.model.ActividadDeTienda;
import java.time.Instant;
import java.util.UUID;

/** HU-015: conteos de envíos y respuestas de una tienda en el esquema {@code solicitudes}. */
public interface ActividadDeTiendaRepository {

    /** Envíos y respuestas de la tienda creados desde {@code desde}. */
    ActividadDeTienda contarDesde(UUID tiendaId, Instant desde);
}
