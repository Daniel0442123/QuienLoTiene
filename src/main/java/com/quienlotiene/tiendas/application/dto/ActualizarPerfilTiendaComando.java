package com.quienlotiene.tiendas.application.dto;

import java.util.List;
import java.util.UUID;

/** Perfil nuevo de la tienda del usuario (HU-009). */
public record ActualizarPerfilTiendaComando(
        UUID duenoId, String nombre, String telefono, List<MiTienda.Franja> horario) {

    public ActualizarPerfilTiendaComando {
        horario = horario == null ? List.of() : List.copyOf(horario);
    }
}
