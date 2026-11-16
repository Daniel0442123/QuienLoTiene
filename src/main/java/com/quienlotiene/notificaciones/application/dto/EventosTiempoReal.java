package com.quienlotiene.notificaciones.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Datos de cada evento del flujo {@code /api/v1/eventos} (contrato del Sprint 2, esquema EventoTiempoReal). El nombre
 * del evento SSE está en {@link #TIPO} de cada record.
 */
public final class EventosTiempoReal {

    private EventosTiempoReal() {}

    /** HU-018, CP-NOT-030-01: tarjeta de la solicitud para la tienda, sin datos del cliente. */
    public record SolicitudNueva(
            UUID solicitudId,
            String producto,
            int cantidad,
            String modalidad,
            boolean esUrgente,
            int distanciaEnMetros,
            Instant fechaVencimiento) {
        public static final String TIPO = "solicitud-nueva";
    }

    /** HU-020, CP-SOL-020-01: para el cliente, solo SI y PARECIDO. */
    public record RespuestaRecibida(UUID solicitudId, UUID respuestaId, String tipo, String nombreTienda) {
        public static final String TIPO = "respuesta-recibida";
    }

    /** HU-021: para las tiendas que recibieron la solicitud, al elegir o al vencer. */
    public record SolicitudCerrada(UUID solicitudId, String estado) {
        public static final String TIPO = "solicitud-cerrada";
    }

    /** CP-SOL-021-02: para las tiendas que respondieron y no fueron elegidas. */
    public record RespuestaNoElegida(UUID solicitudId, UUID respuestaId) {
        public static final String TIPO = "respuesta-no-elegida";
    }

    /** HU-021, diagrama 33 paso 7: para el cliente. */
    public record SolicitudVencida(UUID solicitudId, int respuestasRecibidas) {
        public static final String TIPO = "solicitud-vencida";
    }

    /** HU-024, diagrama 04 paso 9: para la tienda elegida. */
    public record OrdenCreada(UUID ordenId, UUID solicitudId, String producto, long totalEnPesos, String modalidad) {
        public static final String TIPO = "orden-creada";
    }

    /** HU-027 y HU-028: para la otra parte de la orden. */
    public record EstadoOrdenCambiado(UUID ordenId, String estado) {
        public static final String TIPO = "estado-orden-cambiado";
    }
}
