package com.quienlotiene.reputacion.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** La reseña de una orden (HU-034). */
@Schema(name = "ResenaResponse")
public record ResenaVista(
        UUID id,
        UUID ordenId,
        UUID tiendaId,
        int estrellas,
        @Nullable String comentario,
        Instant fecha) {}
