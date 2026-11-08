package com.quienlotiene.solicitudes.infrastructure.web;

import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import com.quienlotiene.solicitudes.application.usecase.PublicarSolicitud;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Solicitudes que publica el cliente (CU-04). */
@Tag(name = "Solicitudes", description = "Publicar y consultar solicitudes")
@RestController
@RequestMapping("/api/v1/solicitudes")
class SolicitudController {

    private final PublicarSolicitud publicarSolicitud;
    private final SolicitudWebMapper solicitudMapper;
    private final UsuarioActual usuarioActual;

    SolicitudController(
            PublicarSolicitud publicarSolicitud, SolicitudWebMapper solicitudMapper, UsuarioActual usuarioActual) {
        this.publicarSolicitud = publicarSolicitud;
        this.solicitudMapper = solicitudMapper;
        this.usuarioActual = usuarioActual;
    }

    @Operation(
            summary = "Publicar solicitud",
            description = "HU-016. Publica \"¿quién tiene X?\" para las tiendas cercanas. Máximo 10 por hora.")
    @ApiResponse(responseCode = "201", description = "Solicitud ABIERTA con su fecha de vencimiento")
    @ApiResponse(responseCode = "400", description = "Falta un dato (GEN-001, SOL-002)")
    @ApiResponse(responseCode = "403", description = "Solo los clientes publican solicitudes (GEN-004)")
    @ApiResponse(
            responseCode = "429",
            description = "Superó las 10 solicitudes de la última hora (SOL-001); Retry-After dice cuándo reintentar")
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    ResponseEntity<SolicitudResponse> publicar(@Valid @RequestBody PublicarSolicitudRequest peticion) {
        var solicitudPublicada = publicarSolicitud.ejecutar(solicitudMapper.aComando(peticion, usuarioActual.id()));
        return ResponseEntity.created(URI.create("/api/v1/solicitudes/" + solicitudPublicada.id()))
                .body(solicitudMapper.aResponse(solicitudPublicada));
    }
}
