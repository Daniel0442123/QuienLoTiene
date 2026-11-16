package com.quienlotiene.solicitudes.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Respuesta tal como la ve la tienda que la dio (HU-019, «Mis respuestas» de HU-021). */
@Schema(name = "RespuestaDeTiendaResponse")
public record RespuestaDeTienda(
        UUID id,
        UUID solicitudId,
        String producto,
        int cantidadSolicitada,
        @Schema(allowableValues = {"SI", "PARECIDO", "NO"}) String tipo,
        @Nullable Long precioEnPesos,
        @Nullable Integer cantidadDisponible,
        @Nullable String descripcionAlternativa,
        @Nullable Integer tiempoEstimadoEnMinutos,
        boolean ofreceDomicilio,
        @Nullable Long costoDomicilioEnPesos,

        @Schema(allowableValues = {"ESPERANDO", "ELEGIDA", "NO_ELEGIDA", "VENCIDA"})
        String estado,

        Instant fechaRespuesta,
        @Nullable UUID ordenId) {}
