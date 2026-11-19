package com.quienlotiene.mensajeria.infrastructure.web;

import com.quienlotiene.mensajeria.application.dto.ChatDeOrden;
import com.quienlotiene.mensajeria.application.dto.MensajeVista;
import com.quienlotiene.mensajeria.application.usecase.ConversarEnOrden;
import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Mini chat entre el cliente y la tienda de una orden (HU-033). */
@Tag(name = "Mensajería", description = "WhatsApp y mini chat de la orden")
@RestController
class ChatController {

    private static final String RUTA = "/api/v1/ordenes/{ordenId}/mensajes";

    private final ConversarEnOrden conversarEnOrden;
    private final UsuarioActual usuarioActual;

    ChatController(ConversarEnOrden conversarEnOrden, UsuarioActual usuarioActual) {
        this.conversarEnOrden = conversarEnOrden;
        this.usuarioActual = usuarioActual;
    }

    @Operation(
            summary = "Leer el chat de la orden",
            description = "HU-033. Del más antiguo al más reciente; soloLectura cuando la orden ya terminó.")
    @ApiResponse(responseCode = "200", description = "Mensajes de la orden")
    @ApiResponse(responseCode = "404", description = "La orden no existe o el usuario no participa (ORD-001)")
    @GetMapping(RUTA)
    @PreAuthorize("hasAnyRole('CLIENTE', 'TIENDA')")
    ChatDeOrden listar(@PathVariable UUID ordenId) {
        return conversarEnOrden.listar(usuarioActual.id(), ordenId);
    }

    @Operation(
            summary = "Escribir en el chat de la orden",
            description = "HU-033. Solo con la orden activa; la otra parte recibe el evento mensaje-recibido.")
    @ApiResponse(responseCode = "201", description = "Mensaje guardado")
    @ApiResponse(responseCode = "400", description = "Mensaje vacío o de más de 1000 caracteres (GEN-001, MEN-003)")
    @ApiResponse(responseCode = "404", description = "La orden no existe o el usuario no participa (ORD-001)")
    @ApiResponse(responseCode = "409", description = "La orden ya terminó: chat en solo lectura (MEN-002)")
    @PostMapping(RUTA)
    @PreAuthorize("hasAnyRole('CLIENTE', 'TIENDA')")
    ResponseEntity<MensajeVista> enviar(@PathVariable UUID ordenId, @Valid @RequestBody EnviarMensajeRequest peticion) {
        var mensaje = conversarEnOrden.enviar(usuarioActual.id(), ordenId, peticion.texto());
        return ResponseEntity.created(URI.create("/api/v1/ordenes/" + ordenId + "/mensajes/" + mensaje.id()))
                .body(mensaje);
    }
}
