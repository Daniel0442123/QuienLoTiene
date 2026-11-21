package com.quienlotiene.administracion.application.dto;

import com.quienlotiene.reputacion.api.ReporteParaAdministrador;
import com.quienlotiene.tiendas.api.TiendaResumen;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Un reporte en el panel del administrador (HU-037) con el nombre y el estado de la tienda, para que la bandeja no
 * tenga que pedir las tiendas aparte. Sin datos personales de quien reporta: solo si fue el cliente o la tienda.
 *
 * @param nombreTienda {@code null} solo si la tienda ya no existe
 * @param estadoTienda {@code null} solo si la tienda ya no existe
 * @param reportesDeLaTienda cuántos reportes tiene en total la tienda de la orden
 */
@Schema(name = "ReporteParaAdministrador")
public record ReporteEnPanel(
        UUID id,
        UUID ordenId,
        UUID tiendaId,
        @Nullable String nombreTienda,

        @Nullable @Schema(allowableValues = {"PENDIENTE", "APROBADA", "RECHAZADA", "SUSPENDIDA"})
        String estadoTienda,

        @Schema(allowableValues = {"CLIENTE", "TIENDA"}) String reportadoPor,

        @Schema(allowableValues = {"DEMORA_ENTREGA", "PRODUCTO_DIFERENTE", "COBRO_DIFERENTE", "OTRO"})
        String motivo,

        @Nullable String descripcion,
        @Schema(allowableValues = {"ABIERTO", "RESUELTO"}) String estado,
        Instant fecha,
        @Nullable String notaResolucion,
        @Nullable Instant fechaResolucion,
        long reportesDeLaTienda) {

    public static ReporteEnPanel de(ReporteParaAdministrador reporte, @Nullable TiendaResumen tienda) {
        return new ReporteEnPanel(
                reporte.id(),
                reporte.ordenId(),
                reporte.tiendaId(),
                tienda == null ? null : tienda.nombre(),
                tienda == null ? null : tienda.estado(),
                reporte.reportadoPor(),
                reporte.motivo(),
                reporte.descripcion(),
                reporte.estado(),
                reporte.fecha(),
                reporte.notaResolucion(),
                reporte.fechaResolucion(),
                reporte.reportesDeLaTienda());
    }
}
