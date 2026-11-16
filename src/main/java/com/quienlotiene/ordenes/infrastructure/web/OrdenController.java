package com.quienlotiene.ordenes.infrastructure.web;

import com.quienlotiene.ordenes.application.dto.CrearOrdenComando;
import com.quienlotiene.ordenes.application.dto.OrdenVista;
import com.quienlotiene.ordenes.application.usecase.CambiarEstadoOrden;
import com.quienlotiene.ordenes.application.usecase.ConfirmarRecepcion;
import com.quienlotiene.ordenes.application.usecase.ConsultarOrden;
import com.quienlotiene.ordenes.application.usecase.CrearOrden;
import com.quienlotiene.ordenes.application.usecase.ListarMisOrdenes;
import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Órdenes del cliente y de la tienda (HU-024, HU-027, HU-028; diagrama 04). */
@Tag(name = "Órdenes", description = "Crear la orden, avanzar su estado y confirmar recepción")
@RestController
@RequestMapping("/api/v1/ordenes")
class OrdenController {

    private final CrearOrden crearOrden;
    private final ConsultarOrden consultarOrden;
    private final ListarMisOrdenes listarMisOrdenes;
    private final CambiarEstadoOrden cambiarEstadoOrden;
    private final ConfirmarRecepcion confirmarRecepcion;
    private final UsuarioActual usuarioActual;

    OrdenController(
            CrearOrden crearOrden,
            ConsultarOrden consultarOrden,
            ListarMisOrdenes listarMisOrdenes,
            CambiarEstadoOrden cambiarEstadoOrden,
            ConfirmarRecepcion confirmarRecepcion,
            UsuarioActual usuarioActual) {
        this.crearOrden = crearOrden;
        this.consultarOrden = consultarOrden;
        this.listarMisOrdenes = listarMisOrdenes;
        this.cambiarEstadoOrden = cambiarEstadoOrden;
        this.confirmarRecepcion = confirmarRecepcion;
        this.usuarioActual = usuarioActual;
    }

    @Operation(
            summary = "Elegir una respuesta y crear la orden",
            description = "HU-024. Total = precio × unidades + domicilio. Una sola elección por solicitud.")
    @ApiResponse(responseCode = "201", description = "Orden CONFIRMADO con el desglose")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (GEN-001) o falta la dirección (ORD-004)")
    @ApiResponse(responseCode = "404", description = "Respuesta (SOL-007) o dirección (CLI-003) no encontrada")
    @ApiResponse(responseCode = "409", description = "La solicitud ya no está abierta (SOL-004)")
    @ApiResponse(
            responseCode = "422",
            description = "Medio de pago no aceptado (ORD-003), respuesta NO (SOL-008) o fuera de cobertura (TIE-010)")
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    ResponseEntity<OrdenVista> crear(@Valid @RequestBody CrearOrdenRequest peticion) {
        var orden = crearOrden.ejecutar(new CrearOrdenComando(
                usuarioActual.id(),
                peticion.respuestaId(),
                peticion.modalidad(),
                peticion.direccionId(),
                peticion.medioPago()));
        return ResponseEntity.created(URI.create("/api/v1/ordenes/" + orden.id()))
                .body(orden);
    }

    @Operation(summary = "Ver mis órdenes", description = "HU-027. Primero las activas. Máximo 50.")
    @ApiResponse(responseCode = "200", description = "Órdenes del usuario")
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'TIENDA')")
    List<OrdenVista> listar() {
        return listarMisOrdenes.ejecutar(usuarioActual.id());
    }

    @Operation(
            summary = "Ver una orden",
            description = "HU-024 y HU-027. La tienda ve los datos del cliente solo mientras la orden está activa.")
    @ApiResponse(responseCode = "200", description = "La orden")
    @ApiResponse(responseCode = "404", description = "No existe o no es del usuario (ORD-001)")
    @GetMapping("/{ordenId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TIENDA')")
    OrdenVista obtener(@PathVariable UUID ordenId) {
        return consultarOrden.ejecutar(usuarioActual.id(), ordenId);
    }

    @Operation(summary = "Avanzar el estado de la orden", description = "HU-027. Solo el paso siguiente válido.")
    @ApiResponse(responseCode = "200", description = "Orden con el estado nuevo")
    @ApiResponse(responseCode = "404", description = "No existe o no es de esta tienda (ORD-001)")
    @ApiResponse(responseCode = "409", description = "Cambio de estado no permitido (ORD-002)")
    @PatchMapping("/{ordenId}/estado")
    @PreAuthorize("hasRole('TIENDA')")
    OrdenVista cambiarEstado(@PathVariable UUID ordenId, @Valid @RequestBody CambiarEstadoOrdenRequest peticion) {
        return cambiarEstadoOrden.ejecutar(usuarioActual.id(), ordenId, peticion.estado());
    }

    @Operation(
            summary = "Confirmar que recibí mi pedido",
            description = "HU-028. Solo con la orden ENTREGADO; queda RESUELTA.")
    @ApiResponse(responseCode = "200", description = "Orden RESUELTA")
    @ApiResponse(responseCode = "404", description = "No existe o es de otro cliente (ORD-001)")
    @ApiResponse(responseCode = "409", description = "Todavía no está ENTREGADO (ORD-002)")
    @PostMapping("/{ordenId}/recepcion")
    @PreAuthorize("hasRole('CLIENTE')")
    OrdenVista confirmarRecepcion(@PathVariable UUID ordenId) {
        return confirmarRecepcion.ejecutar(usuarioActual.id(), ordenId);
    }
}
