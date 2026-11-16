package com.quienlotiene.solicitudes.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de que una solicitud llegó a una tienda (HU-017). Sirve para mostrarle a la tienda sus solicitudes nuevas
 * (HU-018), para aceptar solo respuestas de tiendas que la recibieron (HU-019) y para avisar a su dueño cuando la
 * solicitud se cierra (HU-021).
 *
 * @param duenoId puede ser {@code null} en envíos anteriores al Sprint 2
 */
public record EnvioSolicitud(
        UUID id, UUID solicitudId, UUID tiendaId, UUID duenoId, int distanciaEnMetros, Instant fechaEnvio) {}
