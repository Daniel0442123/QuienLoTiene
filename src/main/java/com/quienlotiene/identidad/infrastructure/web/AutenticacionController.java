package com.quienlotiene.identidad.infrastructure.web;

import com.quienlotiene.identidad.application.dto.SesionIniciada;
import com.quienlotiene.identidad.application.usecase.CerrarSesion;
import com.quienlotiene.identidad.application.usecase.IniciarSesion;
import com.quienlotiene.identidad.application.usecase.RefrescarSesion;
import com.quienlotiene.identidad.application.usecase.RegistrarCliente;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Clock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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
    private final IniciarSesion iniciarSesion;
    private final RefrescarSesion refrescarSesion;
    private final CerrarSesion cerrarSesion;
    private final IdentidadWebMapper identidadMapper;
    private final Clock reloj;

    AutenticacionController(
            RegistrarCliente registrarCliente,
            IniciarSesion iniciarSesion,
            RefrescarSesion refrescarSesion,
            CerrarSesion cerrarSesion,
            IdentidadWebMapper identidadMapper,
            Clock reloj) {
        this.registrarCliente = registrarCliente;
        this.iniciarSesion = iniciarSesion;
        this.refrescarSesion = refrescarSesion;
        this.cerrarSesion = cerrarSesion;
        this.identidadMapper = identidadMapper;
        this.reloj = reloj;
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

    @Operation(
            summary = "Iniciar sesión",
            description = "HU-003. Devuelve el token de acceso de 15 minutos y deja el token de refresco en la cookie "
                    + CookieTokenRefresco.NOMBRE + ".")
    @ApiResponse(responseCode = "200", description = "Sesión iniciada")
    @ApiResponse(responseCode = "401", description = "Correo o frase incorrectos (IDE-008); administrador (IDE-011)")
    @ApiResponse(responseCode = "403", description = "Cuenta suspendida (IDE-009)")
    @PostMapping("/login")
    ResponseEntity<SesionResponse> iniciarSesion(@Valid @RequestBody IniciarSesionRequest peticion) {
        return respuestaConSesion(iniciarSesion.ejecutar(identidadMapper.aComando(peticion)));
    }

    @Operation(
            summary = "Renovar la sesión",
            description = "HU-003. Usa la cookie de refresco, la cambia por una nueva y entrega otro token de acceso.")
    @ApiResponse(responseCode = "200", description = "Sesión renovada")
    @ApiResponse(responseCode = "401", description = "Sin cookie, o token vencido o revocado (IDE-010)")
    @PostMapping("/refresco")
    ResponseEntity<SesionResponse> refrescarSesion(
            @CookieValue(name = CookieTokenRefresco.NOMBRE, required = false) String tokenRefresco) {
        return respuestaConSesion(refrescarSesion.ejecutar(tokenRefresco));
    }

    @Operation(summary = "Cerrar sesión", description = "HU-003. Revoca el token de refresco y borra la cookie.")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada")
    @PostMapping("/logout")
    ResponseEntity<Void> cerrarSesion(
            @CookieValue(name = CookieTokenRefresco.NOMBRE, required = false) String tokenRefresco) {
        cerrarSesion.ejecutar(tokenRefresco);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, CookieTokenRefresco.borrada().toString())
                .build();
    }

    private ResponseEntity<SesionResponse> respuestaConSesion(SesionIniciada sesion) {
        var cookie = CookieTokenRefresco.con(sesion.tokenRefresco(), sesion.fechaExpiracionRefresco(), reloj.instant());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                // La respuesta lleva un token: ningún intermediario debe guardarla
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(identidadMapper.aResponse(sesion));
    }
}
