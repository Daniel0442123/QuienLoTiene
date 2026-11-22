package com.quienlotiene.solicitudes.infrastructure.web;

import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import com.quienlotiene.solicitudes.application.dto.IndicadoresTienda;
import com.quienlotiene.solicitudes.application.usecase.ConsultarIndicadoresTienda;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** «Tu resumen» de la tienda (HU-015, CU-23). */
@Tag(name = "Solicitudes", description = "Publicar, ver y responder solicitudes")
@RestController
class IndicadoresTiendaController {

    private final ConsultarIndicadoresTienda consultarIndicadores;
    private final UsuarioActual usuarioActual;

    IndicadoresTiendaController(ConsultarIndicadoresTienda consultarIndicadores, UsuarioActual usuarioActual) {
        this.consultarIndicadores = consultarIndicadores;
        this.usuarioActual = usuarioActual;
    }

    @Operation(
            summary = "Ver mi resumen de indicadores",
            description = "HU-015. Solicitudes recibidas, respondidas y ganadas en los últimos 7 o 30 días, con los "
                    + "porcentajes «Respondiste» y «Te eligieron» (SC-011 punto 6). Un porcentaje es null cuando su "
                    + "denominador es 0.")
    @ApiResponse(responseCode = "200", description = "Indicadores del periodo; una tienda nueva recibe todo en cero")
    @ApiResponse(responseCode = "400", description = "El periodo no es 7 ni 30 (GEN-001)")
    @GetMapping("/api/v1/tiendas/me/indicadores")
    @PreAuthorize("hasRole('TIENDA')")
    IndicadoresTienda consultar(
            @Parameter(description = "Días hacia atrás", schema = @Schema(allowableValues = {"7", "30"}))
                    @RequestParam(defaultValue = "30")
                    @Pattern(regexp = "7|30", message = "el periodo debe ser 7 o 30 días")
                    String periodo) {
        return consultarIndicadores.ejecutar(usuarioActual.id(), Integer.parseInt(periodo));
    }
}
