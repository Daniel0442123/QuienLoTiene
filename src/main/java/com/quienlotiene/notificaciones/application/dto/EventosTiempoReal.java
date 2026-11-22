package com.quienlotiene.notificaciones.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Datos de cada evento del flujo {@code /api/v1/eventos} (contrato del Sprint 2, esquema EventoTiempoReal). El nombre
 * del evento SSE está en {@code TIPO} de cada record. Se publican en {@code /v3/api-docs} con los nombres del contrato
 * (DEF-006, C-04 A4).
 */
public final class EventosTiempoReal {

    private EventosTiempoReal() {}

    /** HU-018, CP-NOT-030-01: tarjeta de la solicitud para la tienda, sin datos del cliente. */
    @Schema(name = "EventoSolicitudNueva", description = "Mismos datos de la tarjeta de HU-018, sin datos del cliente.")
    public record SolicitudNueva(
            UUID solicitudId,
            String producto,
            int cantidad,

            @Schema(allowableValues = {"RECOGER", "DOMICILIO"})
            String modalidad,

            boolean esUrgente,
            int distanciaEnMetros,
            Instant fechaVencimiento) {
        public static final String TIPO = "solicitud-nueva";
    }

    /** HU-020, CP-SOL-020-01: para el cliente, solo SI y PARECIDO. */
    @Schema(name = "EventoRespuestaRecibida")
    public record RespuestaRecibida(
            UUID solicitudId,
            UUID respuestaId,
            @Schema(allowableValues = {"SI", "PARECIDO"}) String tipo,
            String nombreTienda) {
        public static final String TIPO = "respuesta-recibida";
    }

    /** HU-021: para las tiendas que recibieron la solicitud, al elegir o al vencer. */
    @Schema(name = "EventoSolicitudCerrada")
    public record SolicitudCerrada(
            UUID solicitudId,

            @Schema(allowableValues = {"RESUELTA", "VENCIDA"})
            String estado) {
        public static final String TIPO = "solicitud-cerrada";
    }

    /** CP-SOL-021-02: para las tiendas que respondieron y no fueron elegidas. */
    @Schema(name = "EventoRespuestaNoElegida")
    public record RespuestaNoElegida(UUID solicitudId, UUID respuestaId) {
        public static final String TIPO = "respuesta-no-elegida";
    }

    /** HU-021, diagrama 33 paso 7: para el cliente. */
    @Schema(name = "EventoSolicitudVencida")
    public record SolicitudVencida(UUID solicitudId, int respuestasRecibidas) {
        public static final String TIPO = "solicitud-vencida";
    }

    /** HU-022, diagrama 35 paso 8: para el cliente, «Ampliamos la búsqueda a 5 km». */
    @Schema(name = "EventoRadioAmpliado")
    public record RadioAmpliado(UUID solicitudId, int radioEnMetros, int tiendasNuevas) {
        public static final String TIPO = "radio-ampliado";
    }

    /** HU-024, diagrama 04 paso 9: para la tienda elegida. */
    @Schema(name = "EventoOrdenCreada")
    public record OrdenCreada(
            UUID ordenId,
            UUID solicitudId,
            String producto,
            long totalEnPesos,

            @Schema(allowableValues = {"RECOGER", "DOMICILIO"})
            String modalidad) {
        public static final String TIPO = "orden-creada";
    }

    /** HU-025, HU-027, HU-028 y HU-029: para las dos partes de la orden; {@code motivo} solo al cancelar. */
    @Schema(name = "EventoEstadoOrdenCambiado")
    public record EstadoOrdenCambiado(
            UUID ordenId,

            @Schema(
                    allowableValues = {
                        "CONFIRMADO",
                        "PREPARANDO",
                        "EN_CAMINO",
                        "LISTO_PARA_RECOGER",
                        "ENTREGADO",
                        "RESUELTA",
                        "CANCELADO",
                        "VENCIDA"
                    })
            String estado,

            @Nullable String motivo) {
        public static final String TIPO = "estado-orden-cambiado";
    }

    /** HU-033, CP-MEN-033-01: para la otra parte de la orden; el texto se pide por REST. */
    @Schema(name = "EventoMensajeRecibido")
    public record MensajeRecibido(UUID ordenId, UUID mensajeId) {
        public static final String TIPO = "mensaje-recibido";
    }
}
