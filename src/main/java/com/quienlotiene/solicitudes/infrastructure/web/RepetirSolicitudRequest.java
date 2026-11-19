package com.quienlotiene.solicitudes.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Cuerpo de {@code POST /api/v1/solicitudes/{id}/repeticion} (HU-008): solo la ubicación de ahora. */
public record RepetirSolicitudRequest(
        @NotNull(message = "Comparte tu ubicación") @Valid PublicarSolicitudRequest.UbicacionRequest ubicacion) {}
