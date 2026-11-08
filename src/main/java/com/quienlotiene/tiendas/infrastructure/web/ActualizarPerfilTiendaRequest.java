package com.quienlotiene.tiendas.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.List;

/** Cuerpo de {@code PUT /api/v1/tiendas/me/perfil} (HU-009). */
public record ActualizarPerfilTiendaRequest(
        @NotBlank(message = "Escribe el nombre de la tienda")
        @Size(max = 120, message = "El nombre de la tienda puede tener máximo 120 caracteres")
        String nombre,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(
                regexp = "^\\+?[0-9]{7,15}$",
                message = "El teléfono debe tener entre 7 y 15 dígitos, con + opcional al inicio")
        String telefono,

        @NotNull(message = "Envía el horario; una lista vacía quita el horario")
        @Size(max = 7, message = "El horario tiene máximo una franja por día")
        List<@Valid FranjaRequest> horario) {

    public ActualizarPerfilTiendaRequest {
        // null se deja pasar para que @NotNull responda qué falta
        horario = horario == null ? null : List.copyOf(horario);
    }

    /**
     * @param diaSemana {@code MONDAY} a {@code SUNDAY}
     * @param abre hora local de Colombia, por ejemplo {@code "08:00"}
     */
    public record FranjaRequest(
            @NotNull(message = "Falta el día")
            @Pattern(
                    regexp = "MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY",
                    message = "El día debe ser MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY o SUNDAY")
            String diaSemana,

            @NotNull(message = "Falta la hora de apertura") LocalTime abre,

            @NotNull(message = "Falta la hora de cierre") LocalTime cierra) {}
}
