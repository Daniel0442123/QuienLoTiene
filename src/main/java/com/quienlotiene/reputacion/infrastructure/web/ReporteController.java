package com.quienlotiene.reputacion.infrastructure.web;

import com.quienlotiene.reputacion.application.dto.ReportarComando;
import com.quienlotiene.reputacion.application.dto.ReporteVista;
import com.quienlotiene.reputacion.application.usecase.ReportarProblema;
import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Reportar un problema con una orden (HU-035, CU-13, diagrama 19). */
@Tag(name = "Reputación", description = "Calificar la tienda y reportar problemas")
@RestController
class ReporteController {

    private final ReportarProblema reportarProblema;
    private final UsuarioActual usuarioActual;

    ReporteController(ReportarProblema reportarProblema, UsuarioActual usuarioActual) {
        this.reportarProblema = reportarProblema;
        this.usuarioActual = usuarioActual;
    }

    @Operation(
            summary = "Reportar un problema",
            description = "HU-035. El cliente o la tienda de la orden. Queda ABIERTO para el administrador.")
    @ApiResponse(responseCode = "201", description = "Reporte ABIERTO")
    @ApiResponse(
            responseCode = "400",
            description = "Motivo desconocido, OTRO sin descripción o descripción larga (GEN-001, REP-004)")
    @ApiResponse(responseCode = "404", description = "La orden no existe o el usuario no participa (ORD-001)")
    @PostMapping("/api/v1/ordenes/{ordenId}/reportes")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TIENDA')")
    ResponseEntity<ReporteVista> reportar(@PathVariable UUID ordenId, @Valid @RequestBody ReportarRequest peticion) {
        var reporte = reportarProblema.ejecutar(
                new ReportarComando(usuarioActual.id(), ordenId, peticion.motivo(), peticion.descripcion()));
        return ResponseEntity.created(URI.create("/api/v1/ordenes/" + ordenId + "/reportes/" + reporte.id()))
                .body(reporte);
    }
}
