package com.quienlotiene.solicitudes.infrastructure.web;

import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import com.quienlotiene.solicitudes.application.dto.RespuestaDeTienda;
import com.quienlotiene.solicitudes.application.usecase.ListarMisRespuestas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** «Mis respuestas» de la tienda (HU-021). */
@Tag(name = "Solicitudes", description = "Publicar, ver y responder solicitudes")
@RestController
class MisRespuestasController {

    private final ListarMisRespuestas listarMisRespuestas;
    private final UsuarioActual usuarioActual;

    MisRespuestasController(ListarMisRespuestas listarMisRespuestas, UsuarioActual usuarioActual) {
        this.listarMisRespuestas = listarMisRespuestas;
        this.usuarioActual = usuarioActual;
    }

    @Operation(
            summary = "Ver mis respuestas",
            description = "HU-021. Respuestas SI y PARECIDO de las últimas 24 horas con su estado.")
    @ApiResponse(responseCode = "200", description = "Respuestas de la tienda, la más reciente primero")
    @GetMapping("/api/v1/tiendas/me/respuestas")
    @PreAuthorize("hasRole('TIENDA')")
    List<RespuestaDeTienda> listar() {
        return listarMisRespuestas.ejecutar(usuarioActual.id());
    }
}
