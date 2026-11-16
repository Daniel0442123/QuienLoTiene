package com.quienlotiene.solicitudes.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Respuestas de una solicitud para su cliente (HU-020). Las NO solo cuentan en {@code respuestasRecibidas}.
 *
 * @param respuestasRecibidas todas, incluidas las NO; si es 0 la pantalla muestra el estado vacío
 */
@Schema(name = "RespuestasDeSolicitudResponse")
public record RespuestasDeSolicitud(
        UUID solicitudId,

        @Schema(allowableValues = {"ABIERTA", "RESUELTA", "VENCIDA"})
        String estadoSolicitud,

        int cantidadSolicitada,
        int respuestasRecibidas,
        List<Tarjeta> respuestas) {

    public RespuestasDeSolicitud {
        respuestas = List.copyOf(respuestas);
    }

    /** Tarjeta SI o PARECIDO. Sin teléfono de la tienda (HU-032). */
    @Schema(name = "RespuestaParaClienteResponse")
    public record Tarjeta(
            UUID id,
            UUID tiendaId,
            String nombreTienda,
            @Schema(allowableValues = {"SI", "PARECIDO"}) String tipo,
            boolean esAlternativa,
            @Nullable String descripcionAlternativa,
            long precioEnPesos,
            int cantidadDisponible,
            long subtotalEnPesos,
            boolean ofreceDomicilio,
            @Nullable Long costoDomicilioEnPesos,
            long totalEnPesos,
            int distanciaEnMetros,
            @Nullable Integer tiempoEstimadoEnMinutos,
            Instant fechaRespuesta,
            boolean fueElegida) {}
}
