package com.quienlotiene.solicitudes.application.dto;

import java.util.UUID;

/** Datos de una solicitud nueva (HU-016). El cliente sale del token, no del cuerpo de la petición. */
public record PublicarSolicitudComando(
        UUID clienteId,
        UUID categoriaId,
        String producto,
        int cantidad,
        String modalidad,
        boolean esUrgente,
        double latitud,
        double longitud) {}
