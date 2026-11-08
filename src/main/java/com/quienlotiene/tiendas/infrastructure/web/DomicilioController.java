package com.quienlotiene.tiendas.infrastructure.web;

import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import com.quienlotiene.tiendas.application.dto.Domicilio;
import com.quienlotiene.tiendas.application.usecase.ConfigurarDomicilio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Domicilio de la tienda del usuario (CU-17). */
@Tag(name = "Tiendas", description = "Registro y configuración de las tiendas")
@RestController
@RequestMapping("/api/v1/tiendas/me/domicilio")
@PreAuthorize("hasRole('TIENDA')")
class DomicilioController {

    private final ConfigurarDomicilio configurarDomicilio;
    private final UsuarioActual usuarioActual;

    DomicilioController(ConfigurarDomicilio configurarDomicilio, UsuarioActual usuarioActual) {
        this.configurarDomicilio = configurarDomicilio;
        this.usuarioActual = usuarioActual;
    }

    @Operation(summary = "Ver mi domicilio")
    @GetMapping
    Domicilio consultar() {
        return configurarDomicilio.consultar(usuarioActual.id());
    }

    @Operation(
            summary = "Configurar domicilio",
            description = "HU-010. Activa o apaga el domicilio, radio y tarifa fija.")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (GEN-001) o domicilio inválido (TIE-009)")
    @PutMapping
    Domicilio guardar(@Valid @RequestBody ConfigurarDomicilioRequest peticion) {
        return configurarDomicilio.guardar(
                usuarioActual.id(),
                new Domicilio(
                        peticion.haceDomicilio(),
                        peticion.radioCoberturaEnMetros(),
                        null,
                        peticion.tarifaFijaEnPesos()));
    }

    @Operation(
            summary = "Simular el costo del domicilio",
            description = "CU-17 pasos 7 a 10. Lo que pagaría un cliente con ese subtotal y a esa distancia.")
    @ApiResponse(responseCode = "422", description = "No hace domicilio o el cliente está fuera del radio (TIE-010)")
    @GetMapping("/simulacion")
    SimulacionDomicilioResponse simular(
            @RequestParam @PositiveOrZero(message = "El subtotal no puede ser negativo") long subtotalEnPesos,
            @RequestParam @Min(value = 0, message = "La distancia no puede ser negativa") int distanciaEnMetros) {
        return new SimulacionDomicilioResponse(
                configurarDomicilio.simularCostoEnPesos(usuarioActual.id(), subtotalEnPesos, distanciaEnMetros));
    }

    /** Costo de ejemplo en pesos. */
    record SimulacionDomicilioResponse(long costoDomicilioEnPesos) {}
}
