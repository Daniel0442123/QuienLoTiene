package com.quienlotiene.tiendas.infrastructure.web;

import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import com.quienlotiene.tiendas.application.usecase.ConfigurarMediosPago;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Medios de pago de la tienda del usuario (CU-18). */
@Tag(name = "Tiendas", description = "Registro y configuración de las tiendas")
@RestController
@RequestMapping("/api/v1/tiendas/me/medios-pago")
@PreAuthorize("hasRole('TIENDA')")
class MediosPagoController {

    private final ConfigurarMediosPago configurarMediosPago;
    private final UsuarioActual usuarioActual;

    MediosPagoController(ConfigurarMediosPago configurarMediosPago, UsuarioActual usuarioActual) {
        this.configurarMediosPago = configurarMediosPago;
        this.usuarioActual = usuarioActual;
    }

    @Operation(summary = "Ver mis medios de pago")
    @GetMapping
    MediosPagoResponse consultar() {
        return new MediosPagoResponse(configurarMediosPago.consultar(usuarioActual.id()));
    }

    @Operation(
            summary = "Configurar medios de pago",
            description = "HU-012. Reemplaza la lista: el medio que no se envía queda desactivado.")
    @ApiResponse(responseCode = "400", description = "Ningún medio o uno desconocido (GEN-001, TIE-011)")
    @PutMapping
    MediosPagoResponse guardar(@Valid @RequestBody ConfigurarMediosPagoRequest peticion) {
        return new MediosPagoResponse(configurarMediosPago.guardar(usuarioActual.id(), peticion.mediosPago()));
    }

    /** Medios activos: EFECTIVO, TRANSFERENCIA o DATAFONO. */
    record MediosPagoResponse(List<String> mediosPago) {

        MediosPagoResponse {
            mediosPago = List.copyOf(mediosPago);
        }
    }
}
