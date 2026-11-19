package com.quienlotiene.administracion.infrastructure.web;

import com.quienlotiene.administracion.application.usecase.GestionarReportesYSuspensiones;
import com.quienlotiene.reputacion.api.ReporteParaAdministrador;
import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import com.quienlotiene.tiendas.api.TiendaParaRevision;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Panel del administrador: reportes y suspensiones (HU-037, CU-27). Rol ADMIN (SeguridadConfig). */
@Tag(name = "Administración", description = "Revisión de tiendas, catálogo, reportes y suspensiones")
@RestController
@RequestMapping("/api/v1/admin")
@Validated
class AdministracionReportesController {

    private final GestionarReportesYSuspensiones gestionar;
    private final UsuarioActual usuarioActual;

    AdministracionReportesController(GestionarReportesYSuspensiones gestionar, UsuarioActual usuarioActual) {
        this.gestionar = gestionar;
        this.usuarioActual = usuarioActual;
    }

    @Operation(summary = "Listar reportes", description = "HU-037. Por defecto, los ABIERTO del más antiguo al nuevo.")
    @GetMapping("/reportes")
    List<ReporteParaAdministrador> listarReportes(
            @RequestParam(defaultValue = "ABIERTO")
                    @Pattern(regexp = "ABIERTO|RESUELTO", message = "El estado debe ser ABIERTO o RESUELTO")
                    String estado) {
        return gestionar.listarReportes(estado);
    }

    @Operation(summary = "Resolver un reporte", description = "HU-037. Queda RESUELTO con la nota y auditado.")
    @ApiResponse(responseCode = "200", description = "Reporte RESUELTO")
    @ApiResponse(responseCode = "404", description = "No existe (REP-003)")
    @ApiResponse(responseCode = "409", description = "Ya estaba resuelto (REP-005)")
    @PostMapping("/reportes/{reporteId}/resolucion")
    ReporteParaAdministrador resolver(
            @PathVariable UUID reporteId, @Valid @RequestBody ResolverReporteRequest peticion) {
        return gestionar.resolverReporte(usuarioActual.id(), reporteId, peticion.nota());
    }

    @Operation(
            summary = "Suspender una tienda",
            description = "HU-037. No recibe solicitudes y su dueño no puede iniciar sesión. La interfaz pide "
                    + "confirmación (SC-011). Queda auditado con el motivo.")
    @ApiResponse(responseCode = "200", description = "Tienda SUSPENDIDA")
    @ApiResponse(responseCode = "404", description = "No existe (TIE-006)")
    @ApiResponse(responseCode = "409", description = "No está APROBADA (TIE-004)")
    @PostMapping("/tiendas/{tiendaId}/suspension")
    TiendaParaRevision suspender(@PathVariable UUID tiendaId, @Valid @RequestBody SuspenderTiendaRequest peticion) {
        return gestionar.suspenderTienda(usuarioActual.id(), tiendaId, peticion.motivo());
    }

    @Operation(summary = "Reactivar una tienda", description = "HU-037. Vuelve a APROBADA. Queda auditado.")
    @ApiResponse(responseCode = "200", description = "Tienda APROBADA")
    @ApiResponse(responseCode = "404", description = "No existe (TIE-006)")
    @ApiResponse(responseCode = "409", description = "No está SUSPENDIDA (TIE-004)")
    @PostMapping("/tiendas/{tiendaId}/reactivacion")
    TiendaParaRevision reactivar(@PathVariable UUID tiendaId) {
        return gestionar.reactivarTienda(usuarioActual.id(), tiendaId);
    }
}
