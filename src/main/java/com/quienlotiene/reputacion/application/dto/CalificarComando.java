package com.quienlotiene.reputacion.application.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** HU-034: el cliente califica la tienda de su orden. */
public record CalificarComando(
        UUID clienteId,
        UUID ordenId,
        int estrellas,
        @Nullable String comentario) {}
