package com.quienlotiene.ordenes.application.usecase;

import com.quienlotiene.ordenes.application.dto.OrdenVista;
import com.quienlotiene.ordenes.domain.model.Orden;
import java.util.UUID;

/** Arma lo que ve cada parte de la orden, sin datos del cliente cuando no corresponde. */
final class OrdenVistaMapper {

    private OrdenVistaMapper() {}

    static OrdenVista aVista(Orden orden, UUID usuarioId) {
        return new OrdenVista(
                orden.getId(),
                orden.getSolicitudId(),
                orden.getRespuestaId(),
                orden.getProducto(),
                orden.getCantidad(),
                orden.getPrecioUnitarioEnPesos(),
                orden.subtotalEnPesos(),
                orden.getCostoDomicilioEnPesos(),
                orden.totalEnPesos(),
                orden.getModalidad().name(),
                orden.getMedioPago().name(),
                orden.getEstado().name(),
                orden.esActiva(),
                orden.getFechaCreacion(),
                new OrdenVista.TiendaDeOrden(orden.getTiendaId(), orden.getNombreTienda()),
                orden.muestraDatosDelClienteA(usuarioId) ? clienteDe(orden) : null,
                orden.getHistorial().stream()
                        .map(cambio ->
                                new OrdenVista.CambioEstadoOrden(cambio.estado().name(), cambio.fecha()))
                        .toList(),
                orden.getReservaVence(),
                orden.getMotivoCancelacion(),
                orden.getCanceladaPor() == null ? null : orden.getCanceladaPor().name());
    }

    private static OrdenVista.ClienteDeOrden clienteDe(Orden orden) {
        var direccion = orden.getDireccionEntrega();
        return new OrdenVista.ClienteDeOrden(
                orden.getContactoCliente().nombre(),
                orden.getContactoCliente().telefono(),
                direccion == null
                        ? null
                        : new OrdenVista.DireccionDeEntrega(
                                direccion.nombre(),
                                direccion.direccionEscrita(),
                                direccion.indicaciones(),
                                new OrdenVista.Ubicacion(direccion.latitud(), direccion.longitud())));
    }
}
