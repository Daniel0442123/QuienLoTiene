package com.quienlotiene.solicitudes.infrastructure.web;

import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import com.quienlotiene.solicitudes.application.dto.HistorialSolicitudes;
import com.quienlotiene.solicitudes.application.dto.ResponderSolicitudComando;
import com.quienlotiene.solicitudes.application.dto.RespuestaDeTienda;
import com.quienlotiene.solicitudes.application.dto.RespuestasDeSolicitud;
import com.quienlotiene.solicitudes.application.dto.SolicitudDisponible;
import com.quienlotiene.solicitudes.application.usecase.ConsultarHistorial;
import com.quienlotiene.solicitudes.application.usecase.ConsultarSolicitud;
import com.quienlotiene.solicitudes.application.usecase.ListarRespuestasDeSolicitud;
import com.quienlotiene.solicitudes.application.usecase.ListarSolicitudesDisponibles;
import com.quienlotiene.solicitudes.application.usecase.PublicarSolicitud;
import com.quienlotiene.solicitudes.application.usecase.ResponderSolicitud;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Solicitudes que publica el cliente (CU-04, CU-05) y que responde la tienda (CU-20). */
@Tag(name = "Solicitudes", description = "Publicar, ver y responder solicitudes")
@RestController
@RequestMapping("/api/v1/solicitudes")
class SolicitudController {

    private final PublicarSolicitud publicarSolicitud;
    private final ConsultarHistorial consultarHistorial;
    private final ConsultarSolicitud consultarSolicitud;
    private final ListarSolicitudesDisponibles listarSolicitudesDisponibles;
    private final ResponderSolicitud responderSolicitud;
    private final ListarRespuestasDeSolicitud listarRespuestasDeSolicitud;
    private final SolicitudWebMapper solicitudMapper;
    private final UsuarioActual usuarioActual;

    SolicitudController(
            PublicarSolicitud publicarSolicitud,
            ConsultarHistorial consultarHistorial,
            ConsultarSolicitud consultarSolicitud,
            ListarSolicitudesDisponibles listarSolicitudesDisponibles,
            ResponderSolicitud responderSolicitud,
            ListarRespuestasDeSolicitud listarRespuestasDeSolicitud,
            SolicitudWebMapper solicitudMapper,
            UsuarioActual usuarioActual) {
        this.publicarSolicitud = publicarSolicitud;
        this.consultarHistorial = consultarHistorial;
        this.consultarSolicitud = consultarSolicitud;
        this.listarSolicitudesDisponibles = listarSolicitudesDisponibles;
        this.responderSolicitud = responderSolicitud;
        this.listarRespuestasDeSolicitud = listarRespuestasDeSolicitud;
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

    @Operation(
            summary = "Ver solicitudes nuevas de mi tienda",
            description = "HU-018. ABIERTAS que llegaron a la tienda y que no ha respondido; urgentes primero. "
                    + "Sin datos del cliente; distancia redondeada a 100 m.")
    @ApiResponse(responseCode = "200", description = "Solicitudes nuevas (lista vacía si no hay)")
    @ApiResponse(responseCode = "403", description = "Solo las tiendas (GEN-004)")
    @ApiResponse(responseCode = "404", description = "El usuario no tiene tienda (TIE-006)")
    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('TIENDA')")
    List<SolicitudDisponible> listarDisponibles() {
        return listarSolicitudesDisponibles.ejecutar(usuarioActual.id());
    }

    @Operation(
            summary = "Ver mi historial de solicitudes",
            description = "HU-008 y SC-018. De la más reciente a la más antigua, con cuántas respuestas tiene cada "
                    + "una; estaAbierta marca las de «Mis preguntas». Lista vacía si nunca publicó.")
    @ApiResponse(responseCode = "200", description = "Una página del historial")
    @ApiResponse(responseCode = "400", description = "Página negativa o tamaño fuera de 1 a 50 (SOL-002)")
    @GetMapping("/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    HistorialSolicitudes listarMias(
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamano) {
        return consultarHistorial.listar(usuarioActual.id(), pagina, tamano);
    }

    @Operation(
            summary = "Repetir una solicitud",
            description = "HU-008. Publica otra con el mismo producto, cantidad, categoría, modalidad y urgencia, en "
                    + "la ubicación de ahora. Cuenta para el límite de 10 por hora.")
    @ApiResponse(responseCode = "201", description = "Solicitud nueva ABIERTA")
    @ApiResponse(responseCode = "404", description = "No existe o es de otro cliente (SOL-003)")
    @ApiResponse(responseCode = "429", description = "Superó las 10 solicitudes de la última hora (SOL-001)")
    @PostMapping("/{solicitudId}/repeticion")
    @PreAuthorize("hasRole('CLIENTE')")
    ResponseEntity<SolicitudResponse> repetir(
            @PathVariable UUID solicitudId, @Valid @RequestBody RepetirSolicitudRequest peticion) {
        var nueva = consultarHistorial.repetir(
                usuarioActual.id(),
                solicitudId,
                peticion.ubicacion().latitud(),
                peticion.ubicacion().longitud());
        return ResponseEntity.created(URI.create("/api/v1/solicitudes/" + nueva.id()))
                .body(solicitudMapper.aResponse(nueva));
    }

    @Operation(
            summary = "Ver mi solicitud",
            description = "HU-020 y HU-021. Estado, respuestas recibidas y la orden si ya eligió.")
    @ApiResponse(responseCode = "200", description = "La solicitud")
    @ApiResponse(responseCode = "404", description = "No existe o es de otro cliente (SOL-003)")
    @GetMapping("/{solicitudId}")
    @PreAuthorize("hasRole('CLIENTE')")
    SolicitudResponse obtener(@PathVariable UUID solicitudId) {
        return solicitudMapper.aResponse(consultarSolicitud.ejecutar(usuarioActual.id(), solicitudId));
    }

    @Operation(
            summary = "Responder una solicitud",
            description = "HU-019. SI con precio y unidades; PARECIDO además con la descripción; NO sin datos. "
                    + "Una sola vez por tienda y mientras la solicitud esté abierta.")
    @ApiResponse(responseCode = "201", description = "Respuesta registrada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (GEN-001) o respuesta incompleta (SOL-006)")
    @ApiResponse(responseCode = "404", description = "La solicitud no existe o no llegó a esta tienda (SOL-003)")
    @ApiResponse(responseCode = "409", description = "Ya respondió (SOL-005) o la solicitud se cerró (SOL-004)")
    @PostMapping("/{solicitudId}/respuestas")
    @PreAuthorize("hasRole('TIENDA')")
    ResponseEntity<RespuestaDeTienda> responder(
            @PathVariable UUID solicitudId, @Valid @RequestBody ResponderSolicitudRequest peticion) {
        var respuesta = responderSolicitud.ejecutar(new ResponderSolicitudComando(
                usuarioActual.id(),
                solicitudId,
                peticion.tipo(),
                peticion.precioEnPesos(),
                peticion.cantidadDisponible(),
                peticion.descripcionAlternativa(),
                peticion.tiempoEstimadoEnMinutos()));
        return ResponseEntity.created(URI.create("/api/v1/tiendas/me/respuestas/" + respuesta.id()))
                .body(respuesta);
    }

    @Operation(
            summary = "Ver y ordenar las respuestas de mi solicitud",
            description = "HU-020. Tarjetas SI y PARECIDO; las NO solo cuentan en respuestasRecibidas.")
    @ApiResponse(responseCode = "200", description = "Respuestas ordenadas")
    @ApiResponse(responseCode = "404", description = "No existe o es de otro cliente (SOL-003)")
    @GetMapping("/{solicitudId}/respuestas")
    @PreAuthorize("hasRole('CLIENTE')")
    RespuestasDeSolicitud listarRespuestas(
            @PathVariable UUID solicitudId,
            @Parameter(description = "precio (por defecto), distancia o total con domicilio")
                    @Schema(allowableValues = {"precio", "distancia", "total"})
                    @RequestParam(name = "orden", required = false, defaultValue = "precio")
                    String orden) {
        return listarRespuestasDeSolicitud.ejecutar(usuarioActual.id(), solicitudId, orden);
    }
}
