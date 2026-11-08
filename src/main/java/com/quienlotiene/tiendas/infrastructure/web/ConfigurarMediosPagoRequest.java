package com.quienlotiene.tiendas.infrastructure.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/** Cuerpo de {@code PUT /api/v1/tiendas/me/medios-pago} (HU-012). */
public record ConfigurarMediosPagoRequest(
        @NotEmpty(message = "Elige al menos un medio de pago")
        List<
                        @Pattern(
                                regexp = "EFECTIVO|TRANSFERENCIA|DATAFONO",
                                message = "El medio de pago debe ser EFECTIVO, TRANSFERENCIA o DATAFONO")
                        String>
                mediosPago) {

    public ConfigurarMediosPagoRequest {
        // null se deja pasar para que @NotEmpty responda qué falta
        mediosPago = mediosPago == null ? null : List.copyOf(mediosPago);
    }
}
