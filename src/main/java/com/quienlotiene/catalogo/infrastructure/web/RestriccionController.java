package com.quienlotiene.catalogo.infrastructure.web;

import com.quienlotiene.catalogo.api.CatalogoApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HU-023: aviso de producto restringido mientras el cliente escribe su solicitud (CU-28, mockup de publicar). */
@Tag(name = "Catálogo", description = "Categorías de tiendas y solicitudes")
@RestController
@Validated
class RestriccionController {

    private final CatalogoApi catalogoApi;

    RestriccionController(CatalogoApi catalogoApi) {
        this.catalogoApi = catalogoApi;
    }

    @Operation(
            summary = "Consultar si un producto es restringido",
            description = "HU-023. Compara sin mayúsculas ni tildes con la lista de restringidos de HU-038.")
    @ApiResponse(responseCode = "200", description = "Si es restringido y el aviso que se muestra")
    @ApiResponse(responseCode = "400", description = "Falta el producto (GEN-001)")
    @GetMapping("/api/v1/catalogo/restriccion")
    @PreAuthorize("hasRole('CLIENTE')")
    RestriccionResponse consultar(
            @RequestParam @NotBlank(message = "Escribe el producto") @Size(max = 300, message = "Máximo 300 caracteres")
                    String producto) {
        return catalogoApi.esRestringido(producto)
                ? new RestriccionResponse(true, RestriccionResponse.AVISO)
                : new RestriccionResponse(false, null);
    }
}
