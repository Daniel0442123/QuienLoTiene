package com.quienlotiene.mensajeria.infrastructure.web;

import com.quienlotiene.mensajeria.application.dto.ContactoWhatsapp;
import com.quienlotiene.mensajeria.application.usecase.ContactarPorWhatsapp;
import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Contacto por WhatsApp entre el cliente y la tienda de una orden (HU-032). */
@Tag(name = "Mensajería", description = "Contactar por WhatsApp")
@RestController
class WhatsappController {

    private final ContactarPorWhatsapp contactarPorWhatsapp;
    private final UsuarioActual usuarioActual;

    WhatsappController(ContactarPorWhatsapp contactarPorWhatsapp, UsuarioActual usuarioActual) {
        this.contactarPorWhatsapp = contactarPorWhatsapp;
        this.usuarioActual = usuarioActual;
    }

    @Operation(
            summary = "Obtener el enlace de WhatsApp de la otra parte",
            description = "HU-032. Solo con la orden activa; el cliente recibe el número de la tienda y la tienda "
                    + "el del cliente.")
    @ApiResponse(responseCode = "200", description = "Enlace wa.me listo para abrir")
    @ApiResponse(responseCode = "404", description = "La orden no existe o no es del usuario (ORD-001)")
    @ApiResponse(responseCode = "409", description = "La orden ya no está activa (MEN-001)")
    @GetMapping("/api/v1/ordenes/{ordenId}/whatsapp")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TIENDA')")
    ContactoWhatsapp obtener(@PathVariable UUID ordenId) {
        return contactarPorWhatsapp.ejecutar(usuarioActual.id(), ordenId);
    }
}
