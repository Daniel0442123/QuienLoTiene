package com.quienlotiene.solicitudes.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de que una solicitud llegó a una tienda (HU-017). Sirve para mostrarle a la tienda sus solicitudes nuevas
 * (HU-018) y para aceptar solo respuestas de tiendas que la recibieron (HU-019).
 */
public record EnvioSolicitud(UUID id, UUID solicitudId, UUID tiendaId, int distanciaEnMetros, Instant fechaEnvio) {}
