package com.quienlotiene.tiendas.infrastructure.web;

import com.quienlotiene.tiendas.application.dto.TiendaPublica;
import com.quienlotiene.tiendas.application.usecase.VerDetalleTienda;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Detalle público de una tienda (HU-014, CU-06). Pide sesión, sin rol especial. */
@Tag(name = "Tiendas", description = "Detalle público de una tienda")
@RestController
@RequestMapping("/api/v1/tiendas")
class DetalleTiendaController {

    private final VerDetalleTienda verDetalleTienda;

    DetalleTiendaController(VerDetalleTienda verDetalleTienda) {
        this.verDetalleTienda = verDetalleTienda;
    }

    @Operation(
            summary = "Ver el detalle de una tienda",
            description = "HU-014. Horario, ubicación, teléfono, domicilio y medios de pago de una tienda APROBADA. "
                    + "Sin RUT ni documentos. Las reseñas están en /api/v1/tiendas/{tiendaId}/resenas.")
    @ApiResponse(responseCode = "200", description = "Detalle de la tienda")
    @ApiResponse(responseCode = "404", description = "No existe o no está aprobada (TIE-006)")
    @GetMapping("/{tiendaId}")
    TiendaPublica obtener(@PathVariable UUID tiendaId) {
        return verDetalleTienda.ejecutar(tiendaId);
    }
}
