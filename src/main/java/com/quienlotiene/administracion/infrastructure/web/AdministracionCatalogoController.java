package com.quienlotiene.administracion.infrastructure.web;

import com.quienlotiene.administracion.application.usecase.GestionarCatalogoAdministrado;
import com.quienlotiene.catalogo.api.CategoriaAdministrada;
import com.quienlotiene.catalogo.api.ProductoRestringido;
import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Panel del administrador: categorías y productos restringidos (HU-038, CU-28). Rol ADMIN (SeguridadConfig). */
@Tag(name = "Administración", description = "Revisión de tiendas, catálogo, reportes y suspensiones")
@RestController
@RequestMapping("/api/v1/admin")
class AdministracionCatalogoController {

    private final GestionarCatalogoAdministrado gestionarCatalogo;
    private final UsuarioActual usuarioActual;

    AdministracionCatalogoController(GestionarCatalogoAdministrado gestionarCatalogo, UsuarioActual usuarioActual) {
        this.gestionarCatalogo = gestionarCatalogo;
        this.usuarioActual = usuarioActual;
    }

    @Operation(summary = "Listar todas las categorías", description = "HU-038. Activas e inactivas.")
    @GetMapping("/categorias")
    List<CategoriaAdministrada> listarCategorias() {
        return gestionarCatalogo.listarCategorias();
    }

    @Operation(
            summary = "Activar o desactivar una categoría",
            description = "HU-038. Una inactiva deja de salir al publicar y al registrar tiendas. Queda auditado.")
    @ApiResponse(responseCode = "200", description = "La categoría con su estado nuevo")
    @ApiResponse(responseCode = "404", description = "No existe (CAT-001)")
    @PatchMapping("/categorias/{categoriaId}")
    CategoriaAdministrada cambiarCategoria(
            @PathVariable UUID categoriaId, @Valid @RequestBody CambiarCategoriaRequest peticion) {
        return gestionarCatalogo.cambiarActiva(usuarioActual.id(), categoriaId, peticion.activa());
    }

    @Operation(summary = "Listar productos restringidos", description = "HU-038. Palabras clave ordenadas.")
    @GetMapping("/productos-restringidos")
    List<ProductoRestringido> listarRestringidos() {
        return gestionarCatalogo.listarRestringidos();
    }

    @Operation(
            summary = "Agregar un producto restringido",
            description = "HU-038. Se guarda en minúsculas y sin tildes. Queda auditado.")
    @ApiResponse(responseCode = "201", description = "La palabra agregada")
    @ApiResponse(responseCode = "400", description = "Muy corta o muy larga (GEN-001, CAT-003)")
    @ApiResponse(responseCode = "409", description = "Ya está en la lista (CAT-002)")
    @PostMapping("/productos-restringidos")
    ResponseEntity<ProductoRestringido> agregarRestringido(@Valid @RequestBody AgregarRestringidoRequest peticion) {
        var producto = gestionarCatalogo.agregarRestringido(usuarioActual.id(), peticion.palabraClave());
        return ResponseEntity.created(URI.create("/api/v1/admin/productos-restringidos/" + producto.id()))
                .body(producto);
    }

    @Operation(summary = "Quitar un producto restringido", description = "HU-038. Queda auditado.")
    @ApiResponse(responseCode = "204", description = "Quitado")
    @ApiResponse(responseCode = "404", description = "No existe (CAT-001)")
    @DeleteMapping("/productos-restringidos/{productoRestringidoId}")
    ResponseEntity<Void> quitarRestringido(@PathVariable UUID productoRestringidoId) {
        gestionarCatalogo.quitarRestringido(usuarioActual.id(), productoRestringidoId);
        return ResponseEntity.noContent().build();
    }
}
