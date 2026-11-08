package com.quienlotiene.clientes.infrastructure.web;

import com.quienlotiene.clientes.application.dto.DatosDireccion;
import com.quienlotiene.clientes.application.usecase.GestionarDirecciones;
import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Direcciones del cliente que inició sesión (CU-03). */
@Tag(name = "Clientes", description = "Direcciones de entrega del cliente")
@RestController
@RequestMapping("/api/v1/clientes/me/direcciones")
@PreAuthorize("hasRole('CLIENTE')")
class DireccionController {

    private final GestionarDirecciones gestionarDirecciones;
    private final UsuarioActual usuarioActual;

    DireccionController(GestionarDirecciones gestionarDirecciones, UsuarioActual usuarioActual) {
        this.gestionarDirecciones = gestionarDirecciones;
        this.usuarioActual = usuarioActual;
    }

    @Operation(summary = "Listar mis direcciones")
    @GetMapping
    List<DatosDireccion> listar() {
        return gestionarDirecciones.listar(usuarioActual.id());
    }

    @Operation(summary = "Guardar una dirección", description = "HU-007 (CP-CLI-007-01). Máximo 10 por cliente.")
    @ApiResponse(responseCode = "201", description = "Dirección guardada")
    @ApiResponse(responseCode = "400", description = "Falta un dato (GEN-001, CLI-001)")
    @ApiResponse(responseCode = "422", description = "Ya tiene 10 direcciones (CLI-002)")
    @PostMapping
    ResponseEntity<DatosDireccion> crear(@Valid @RequestBody DireccionRequest peticion) {
        var direccion = gestionarDirecciones.crear(usuarioActual.id(), aDatos(peticion));
        return ResponseEntity.created(URI.create("/api/v1/clientes/me/direcciones/" + direccion.id()))
                .body(direccion);
    }

    @Operation(summary = "Editar una dirección")
    @ApiResponse(responseCode = "404", description = "No existe o es de otro cliente (CLI-003)")
    @PutMapping("/{direccionId}")
    DatosDireccion editar(@PathVariable UUID direccionId, @Valid @RequestBody DireccionRequest peticion) {
        return gestionarDirecciones.editar(usuarioActual.id(), direccionId, aDatos(peticion));
    }

    @Operation(summary = "Borrar una dirección")
    @ApiResponse(responseCode = "204", description = "Dirección borrada")
    @ApiResponse(responseCode = "404", description = "No existe o es de otro cliente (CLI-003)")
    @DeleteMapping("/{direccionId}")
    ResponseEntity<Void> borrar(@PathVariable UUID direccionId) {
        gestionarDirecciones.borrar(usuarioActual.id(), direccionId);
        return ResponseEntity.noContent().build();
    }

    private static DatosDireccion aDatos(DireccionRequest peticion) {
        return new DatosDireccion(
                null,
                peticion.nombre(),
                peticion.ubicacion().latitud(),
                peticion.ubicacion().longitud(),
                peticion.direccionEscrita(),
                peticion.indicaciones(),
                peticion.telefono(),
                null);
    }
}
