package com.quienlotiene.solicitudes.api;

import java.time.Instant;
import java.util.UUID;

/**
 * Se publica cuando un cliente publica una solicitud (HU-016, diagrama 03 paso 7). Lo escuchan la difusión a tiendas
 * elegibles (HU-017) y las notificaciones. No lleva datos personales del cliente: solo su id.
 */
public record SolicitudPublicadaEvent(
        UUID solicitudId,
        UUID clienteId,
        UUID categoriaId,
        String producto,
        int cantidad,
        String modalidad,
        boolean esUrgente,
        double latitud,
        double longitud,
        int radioEnMetros,
        Instant fechaVencimiento) {}
