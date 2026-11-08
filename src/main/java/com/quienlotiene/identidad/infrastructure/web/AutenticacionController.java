package com.quienlotiene.identidad.infrastructure.web;

import com.quienlotiene.identidad.application.usecase.RegistrarCliente;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Registro, inicio y cierre de sesión (CU-01, CU-02). Rutas públicas: no piden token. */
@Tag(name = "Autenticación", description = "Registro de clientes, inicio y cierre de sesión")
@RestController
@RequestMapping("/api/v1/auth")
class AutenticacionController {

    /** Recurso del usuario actual una vez que inicie sesión (PATCH /api/v1/usuarios/me, diagrama 12). */
    private static final URI UBICACION_USUARIO_ACTUAL = URI.create("/api/v1/usuarios/me");

    private final RegistrarCliente registrarCliente;
    private final IdentidadWebMapper identidadMapper;

    AutenticacionController(RegistrarCliente registrarCliente, IdentidadWebMapper identidadMapper) {
        this.registrarCliente = registrarCliente;
        this.identidadMapper = identidadMapper;
    }

    @Operation(summary = "Registrar cliente", description = "HU-001. Crea una cuenta de cliente activa.")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos (GEN-001) o frase débil (IDE-002, IDE-003, IDE-004)")
    @ApiResponse(responseCode = "409", description = "El correo ya tiene una cuenta (IDE-001)")
    @PostMapping("/registro")
    ResponseEntity<UsuarioRegistradoResponse> registrar(@Valid @RequestBody RegistrarClienteRequest peticion) {
        var clienteRegistrado = registrarCliente.ejecutar(identidadMapper.aComando(peticion));
        return ResponseEntity.created(UBICACION_USUARIO_ACTUAL).body(identidadMapper.aResponse(clienteRegistrado));
    }
}
