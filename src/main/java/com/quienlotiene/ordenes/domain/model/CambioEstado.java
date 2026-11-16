package com.quienlotiene.ordenes.domain.model;

import java.time.Instant;

/** Un paso del historial de la orden ({@code ordenes.historial_estado}). */
public record CambioEstado(EstadoOrden estado, Instant fecha) {}
