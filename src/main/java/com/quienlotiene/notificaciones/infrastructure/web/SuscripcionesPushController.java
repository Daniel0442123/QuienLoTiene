package com.quienlotiene.notificaciones.infrastructure.web;

import com.quienlotiene.notificaciones.application.dto.ConfiguracionPush;
import com.quienlotiene.notificaciones.application.usecase.GestionarSuscripcionesPush;
import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Suscripciones Web Push del navegador (HU-031, CU-24, diagrama 28). */
@Tag(name = "Eventos", description = "Notificaciones en tiempo real (SSE)")
@Validated
@RestController
class SuscripcionesPushController {

    private static final String RUTA = "/api/v1/notificaciones/suscripciones";

    private final GestionarSuscripcionesPush gestionarSuscripcionesPush;
    private final UsuarioActual usuarioActual;

    SuscripcionesPushController(GestionarSuscripcionesPush gestionarSuscripcionesPush, UsuarioActual usuarioActual) {
        this.gestionarSuscripcionesPush = gestionarSuscripcionesPush;
        this.usuarioActual = usuarioActual;
    }

    @Operation(
            summary = "Configuración de Web Push",
            description = "HU-031. La clave pública VAPID para PushManager.subscribe; si habilitado es false, el "
                    + "frontend no pide el permiso.")
    @ApiResponse(responseCode = "200", description = "Configuración")
    @GetMapping("/api/v1/notificaciones/configuracion-push")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TIENDA')")
    ConfiguracionPush configuracion() {
        return gestionarSuscripcionesPush.configuracion();
    }

    @Operation(
            summary = "Suscribir este navegador a los avisos push",
            description = "HU-031. Recibe PushSubscription.toJSON(). Si el endpoint ya estaba, se reemplaza.")
    @ApiResponse(responseCode = "201", description = "Suscripción guardada")
    @ApiResponse(
            responseCode = "400",
            description = "Endpoint que no es de un servicio push conocido o claves inválidas (GEN-001, NOT-001)")
    @PostMapping(RUTA)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CLIENTE', 'TIENDA')")
    void suscribir(@Valid @RequestBody SuscribirPushRequest peticion) {
        gestionarSuscripcionesPush.suscribir(
                usuarioActual.id(),
                peticion.endpoint(),
                peticion.keys().p256dh(),
                peticion.keys().auth());
    }

    @Operation(
            summary = "Dar de baja este navegador",
            description = "HU-031. Idempotente: si no existe o es de otro usuario, responde 204 igual.")
    @ApiResponse(responseCode = "204", description = "Suscripción borrada")
    @DeleteMapping(RUTA)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CLIENTE', 'TIENDA')")
    void cancelar(@RequestParam @NotBlank @Size(max = 1000) String endpoint) {
        gestionarSuscripcionesPush.cancelar(usuarioActual.id(), endpoint);
    }
}
