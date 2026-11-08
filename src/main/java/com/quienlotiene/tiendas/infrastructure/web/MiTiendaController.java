package com.quienlotiene.tiendas.infrastructure.web;

import com.quienlotiene.shared.infrastructure.security.UsuarioActual;
import com.quienlotiene.tiendas.application.dto.ActualizarPerfilTiendaComando;
import com.quienlotiene.tiendas.application.dto.MiTienda;
import com.quienlotiene.tiendas.application.usecase.ActualizarPerfilTienda;
import com.quienlotiene.tiendas.application.usecase.ConsultarMiTienda;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** La tienda del usuario que inició sesión ({@code me}): perfil, horario y, más adelante, apertura y domicilio. */
@Tag(name = "Tiendas", description = "Registro y configuración de las tiendas")
@RestController
@RequestMapping("/api/v1/tiendas/me")
@PreAuthorize("hasRole('TIENDA')")
class MiTiendaController {

    private final ConsultarMiTienda consultarMiTienda;
    private final ActualizarPerfilTienda actualizarPerfilTienda;
    private final UsuarioActual usuarioActual;

    MiTiendaController(
            ConsultarMiTienda consultarMiTienda,
            ActualizarPerfilTienda actualizarPerfilTienda,
            UsuarioActual usuarioActual) {
        this.consultarMiTienda = consultarMiTienda;
        this.actualizarPerfilTienda = actualizarPerfilTienda;
        this.usuarioActual = usuarioActual;
    }

    @Operation(summary = "Ver mi tienda", description = "Estado, motivo de rechazo si lo hay, perfil y horario.")
    @ApiResponse(responseCode = "404", description = "El usuario no tiene tienda (TIE-006)")
    @GetMapping
    MiTienda consultar() {
        return consultarMiTienda.ejecutar(usuarioActual.id());
    }

    @Operation(
            summary = "Actualizar perfil y horario",
            description = "HU-009. Nombre, teléfono y una franja por día en hora de Colombia; los días que no se "
                    + "envían quedan cerrados.")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (GEN-001) u horario inválido (TIE-007)")
    @PutMapping("/perfil")
    MiTienda actualizarPerfil(@Valid @RequestBody ActualizarPerfilTiendaRequest peticion) {
        return actualizarPerfilTienda.ejecutar(new ActualizarPerfilTiendaComando(
                usuarioActual.id(),
                peticion.nombre(),
                peticion.telefono(),
                peticion.horario().stream()
                        .map(franja -> new MiTienda.Franja(franja.diaSemana(), franja.abre(), franja.cierra()))
                        .toList()));
    }
}
