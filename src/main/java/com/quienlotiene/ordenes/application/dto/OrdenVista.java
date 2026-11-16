package com.quienlotiene.ordenes.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Una orden tal como la ve quien la pide (HU-024, HU-027, HU-028).
 *
 * @param producto lo que se entrega; con PARECIDO, la descripción de la alternativa
 * @param cliente solo para la tienda y solo con la orden activa (CP-ORD-024-02, CP-ORD-028-01)
 */
@Schema(name = "OrdenResponse")
public record OrdenVista(
        UUID id,
        UUID solicitudId,
        UUID respuestaId,
        String producto,
        int cantidad,
        long precioUnitarioEnPesos,
        long subtotalEnPesos,
        long costoDomicilioEnPesos,
        long totalEnPesos,
        @Schema(allowableValues = {"RECOGER", "DOMICILIO"}) String modalidad,

        @Schema(allowableValues = {"EFECTIVO", "TRANSFERENCIA", "DATAFONO"})
        String medioPago,

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

        boolean esActiva,
        Instant fechaCreacion,
        TiendaDeOrden tienda,
        @Nullable ClienteDeOrden cliente,
        List<CambioEstadoOrden> historial) {

    public OrdenVista {
        historial = List.copyOf(historial);
    }

    public record TiendaDeOrden(UUID id, String nombre) {}

    public record ClienteDeOrden(
            String nombre, String telefono, @Nullable DireccionDeEntrega direccion) {

        @Override
        public String toString() {
            return "ClienteDeOrden[***]";
        }
    }

    public record DireccionDeEntrega(
            String nombre,
            String direccionEscrita,
            @Nullable String indicaciones,
            Ubicacion ubicacion) {

        @Override
        public String toString() {
            return "DireccionDeEntrega[***]";
        }
    }

    public record Ubicacion(double latitud, double longitud) {}

    public record CambioEstadoOrden(String estado, Instant fecha) {}
}
