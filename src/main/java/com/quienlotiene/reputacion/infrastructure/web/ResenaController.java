package com.quienlotiene.reputacion.infrastructure.web;

import com.quienlotiene.reputacion.application.dto.CalificarComando;
import com.quienlotiene.reputacion.application.dto.ResenaVista;
import com.quienlotiene.reputacion.application.usecase.CalificarTienda;
import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Reseña de una orden (HU-034, CU-10, diagrama 16). */
@Tag(name = "Reputación", description = "Calificar la tienda y reportar problemas")
@RestController
@RequestMapping("/api/v1/ordenes/{ordenId}/resena")
class ResenaController {

    private final CalificarTienda calificarTienda;
    private final UsuarioActual usuarioActual;

    ResenaController(CalificarTienda calificarTienda, UsuarioActual usuarioActual) {
        this.calificarTienda = calificarTienda;
        this.usuarioActual = usuarioActual;
    }

    @Operation(
            summary = "Calificar la tienda",
            description = "HU-034. Solo el cliente de una orden RESUELTA y una vez por orden.")
    @ApiResponse(responseCode = "201", description = "Reseña publicada")
    @ApiResponse(responseCode = "400", description = "Estrellas fuera de 1 a 5 o comentario largo (GEN-001)")
    @ApiResponse(responseCode = "404", description = "La orden no existe o no es del usuario (ORD-001)")
    @ApiResponse(responseCode = "409", description = "La orden ya tiene reseña (REP-002)")
    @ApiResponse(responseCode = "422", description = "La orden no está RESUELTA (REP-001)")
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    ResponseEntity<ResenaVista> calificar(@PathVariable UUID ordenId, @Valid @RequestBody CalificarRequest peticion) {
        var resena = calificarTienda.ejecutar(
                new CalificarComando(usuarioActual.id(), ordenId, peticion.estrellas(), peticion.comentario()));
        return ResponseEntity.created(URI.create("/api/v1/ordenes/" + ordenId + "/resena"))
                .body(resena);
    }

    @Operation(summary = "Ver la reseña de la orden", description = "HU-034. Para el cliente y la tienda de la orden.")
    @ApiResponse(responseCode = "200", description = "La reseña")
    @ApiResponse(
            responseCode = "404",
            description = "La orden no es del usuario (ORD-001) o todavía no tiene reseña (REP-003)")
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'TIENDA')")
    ResenaVista obtener(@PathVariable UUID ordenId) {
        return calificarTienda.consultar(usuarioActual.id(), ordenId);
    }
}
