package com.quienlotiene.administracion.infrastructure.web;

import com.quienlotiene.administracion.application.usecase.RevisarTienda;
import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import com.quienlotiene.tiendas.api.TiendaParaRevision;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Panel del administrador para revisar tiendas (CU-26). Toda la ruta exige rol ADMIN (SeguridadConfig). */
@Tag(name = "Administración", description = "Revisión de tiendas")
@RestController
@RequestMapping("/api/v1/admin/tiendas")
class AdministracionTiendasController {

    private final RevisarTienda revisarTienda;
    private final UsuarioActual usuarioActual;

    AdministracionTiendasController(RevisarTienda revisarTienda, UsuarioActual usuarioActual) {
        this.revisarTienda = revisarTienda;
        this.usuarioActual = usuarioActual;
    }

    @Operation(summary = "Listar tiendas por estado", description = "HU-036. Por defecto, las PENDIENTE de revisión.")
    @GetMapping
    List<TiendaParaRevision> listar(
            @RequestParam(defaultValue = "PENDIENTE")
                    @Pattern(
                            regexp = "PENDIENTE|APROBADA|RECHAZADA|SUSPENDIDA",
                            message = "El estado debe ser PENDIENTE, APROBADA, RECHAZADA o SUSPENDIDA")
                    String estado) {
        return revisarTienda.listar(estado);
    }

    @Operation(summary = "Descargar un documento", description = "HU-036. Queda en la auditoría.")
    @ApiResponse(responseCode = "404", description = "La tienda o el documento no existe (TIE-006)")
    @GetMapping("/{tiendaId}/documentos/{documentoId}")
    ResponseEntity<byte[]> descargarDocumento(@PathVariable UUID tiendaId, @PathVariable UUID documentoId) {
        var documento = revisarTienda.descargarDocumento(usuarioActual.id(), tiendaId, documentoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(documento.tipoContenido()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(documento.nombreArchivo())
                                .build()
                                .toString())
                // Datos personales: ni el navegador ni un proxy deben guardar copia
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(documento.contenido());
    }

    @Operation(summary = "Aprobar tienda", description = "HU-036 (CP-ADM-036-01). La tienda queda APROBADA.")
    @ApiResponse(responseCode = "409", description = "La tienda no está PENDIENTE (TIE-004)")
    @PostMapping("/{tiendaId}/aprobacion")
    TiendaParaRevision aprobar(@PathVariable UUID tiendaId) {
        return revisarTienda.aprobar(usuarioActual.id(), tiendaId);
    }

    @Operation(
            summary = "Rechazar tienda",
            description = "HU-036 (CP-ADM-036-02). La tienda queda RECHAZADA con el motivo.")
    @ApiResponse(responseCode = "400", description = "Falta el motivo (GEN-001, TIE-005)")
    @ApiResponse(responseCode = "409", description = "La tienda no está PENDIENTE (TIE-004)")
    @PostMapping("/{tiendaId}/rechazo")
    TiendaParaRevision rechazar(@PathVariable UUID tiendaId, @Valid @RequestBody RechazarTiendaRequest peticion) {
        return revisarTienda.rechazar(usuarioActual.id(), tiendaId, peticion.motivo());
    }
}
