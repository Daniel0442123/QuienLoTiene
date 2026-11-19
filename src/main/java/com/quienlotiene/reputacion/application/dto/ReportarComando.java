package com.quienlotiene.reputacion.application.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** HU-035: el cliente o la tienda de una orden reporta un problema. */
public record ReportarComando(
        UUID usuarioId,
        UUID ordenId,
        String motivo,
        @Nullable String descripcion) {}
