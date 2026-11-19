package com.quienlotiene.reputacion.infrastructure.web;

import com.quienlotiene.reputacion.application.dto.ReputacionTienda;
import com.quienlotiene.reputacion.application.usecase.ConsultarReputacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Promedio y reseñas de una tienda para su detalle (HU-014, HU-034). Pide sesión, sin rol especial. */
@Tag(name = "Reputación", description = "Calificar la tienda y reportar problemas")
@RestController
class ReputacionTiendaController {

    private final ConsultarReputacion consultarReputacion;

    ReputacionTiendaController(ConsultarReputacion consultarReputacion) {
        this.consultarReputacion = consultarReputacion;
    }

    @Operation(
            summary = "Ver la calificación de una tienda",
            description = "HU-014. Promedio, total y las 10 reseñas más recientes, sin el nombre del cliente.")
    @ApiResponse(responseCode = "200", description = "Calificación; promedio 0 si no tiene reseñas")
    @GetMapping("/api/v1/tiendas/{tiendaId}/resenas")
    ReputacionTienda obtener(@PathVariable UUID tiendaId) {
        return consultarReputacion.ejecutar(tiendaId);
    }
}
