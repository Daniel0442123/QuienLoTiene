package com.quienlotiene.catalogo.infrastructure.web;

import com.quienlotiene.catalogo.application.usecase.ListarCategorias;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Categorías públicas: el formulario de registro de tienda las necesita antes de que exista una sesión. */
@Tag(name = "Catálogo", description = "Categorías de tiendas y solicitudes")
@RestController
@RequestMapping("/api/v1/categorias")
class CategoriaController {

    private final ListarCategorias listarCategorias;

    CategoriaController(ListarCategorias listarCategorias) {
        this.listarCategorias = listarCategorias;
    }

    @Operation(
            summary = "Listar categorías",
            description = "HU-002 y HU-016. Categorías activas ordenadas por nombre. No pide sesión.")
    @ApiResponse(responseCode = "200", description = "Categorías activas")
    @GetMapping
    List<CategoriaResponse> listar() {
        return listarCategorias.ejecutar().stream()
                .map(categoria -> new CategoriaResponse(categoria.id(), categoria.nombre()))
                .toList();
    }
}
