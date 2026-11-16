package com.quienlotiene.solicitudes.application.dto;

import java.util.UUID;

/** Respuesta de la tienda a una solicitud (HU-019). El dueño sale del token, no del cuerpo. */
public record ResponderSolicitudComando(
        UUID duenoId,
        UUID solicitudId,
        String tipo,
        Long precioEnPesos,
        Integer cantidadDisponible,
        String descripcionAlternativa,
        Integer tiempoEstimadoEnMinutos) {}
