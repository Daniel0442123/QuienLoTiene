package com.quienlotiene.identidad.infrastructure.web;

import com.quienlotiene.identidad.application.dto.PerfilUsuario;
import com.quienlotiene.identidad.application.usecase.GestionarPerfilUsuario;
import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Perfil del usuario que inició sesión, de cualquier rol (CU-03). */
@Tag(name = "Usuarios", description = "Perfil del usuario actual")
@RestController
@RequestMapping("/api/v1/usuarios/me")
class UsuarioController {

    private final GestionarPerfilUsuario gestionarPerfil;
    private final UsuarioActual usuarioActual;

    UsuarioController(GestionarPerfilUsuario gestionarPerfil, UsuarioActual usuarioActual) {
        this.gestionarPerfil = gestionarPerfil;
        this.usuarioActual = usuarioActual;
    }

    @Operation(summary = "Ver mi perfil")
    @GetMapping
    PerfilUsuario consultar() {
        return gestionarPerfil.consultar(usuarioActual.id());
    }

    @Operation(summary = "Editar mi nombre y teléfono", description = "HU-007 (CU-03 pasos 14 a 16).")
    @PatchMapping
    PerfilUsuario actualizar(@Valid @RequestBody ActualizarPerfilRequest peticion) {
        return gestionarPerfil.actualizar(usuarioActual.id(), peticion.nombre(), peticion.telefono());
    }
}
