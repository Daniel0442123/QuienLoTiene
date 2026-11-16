package com.quienlotiene.notificaciones.infrastructure.web;

import com.quienlotiene.notificaciones.infrastructure.sse.EmisoresSse;
import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** HU-030 Notificaciones en tiempo real (CU-14, diagrama 20 pasos 1 a 4). */
@Tag(name = "Eventos", description = "Notificaciones en tiempo real (SSE)")
@RestController
class EventosController {

    private final EmisoresSse emisores;
    private final UsuarioActual usuarioActual;

    EventosController(EmisoresSse emisores, UsuarioActual usuarioActual) {
        this.emisores = emisores;
        this.usuarioActual = usuarioActual;
    }

    @Operation(
            summary = "Abrir el flujo de eventos en tiempo real",
            description = "HU-030. text/event-stream con el JWT en Authorization. Hasta 5 conexiones por usuario "
                    + "(SC-014), latido ':latido' cada 25 s y reenvío de lo perdido en 5 minutos con Last-Event-ID. "
                    + "Tipos: solicitud-nueva, respuesta-recibida, solicitud-cerrada, respuesta-no-elegida, "
                    + "solicitud-vencida, orden-creada y estado-orden-cambiado (docs/api/contrato-sprint-2.yaml).")
    @ApiResponse(responseCode = "200", description = "Conexión abierta")
    @ApiResponse(responseCode = "401", description = "Sin sesión (GEN-003)")
    @GetMapping(path = "/api/v1/eventos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter abrir(
            @Parameter(description = "Último id recibido, para reenviar lo perdido")
                    @RequestHeader(name = "Last-Event-ID", required = false)
                    String ultimoIdRecibido) {
        return emisores.conectar(usuarioActual.id(), ultimoIdRecibido);
    }
}
