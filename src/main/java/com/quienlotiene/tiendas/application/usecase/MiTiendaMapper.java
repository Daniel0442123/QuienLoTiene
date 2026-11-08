package com.quienlotiene.tiendas.application.usecase;

import com.quienlotiene.tiendas.application.dto.MiTienda;
import com.quienlotiene.tiendas.domain.model.Tienda;

/** Convierte la tienda del dominio en lo que ve su dueño. */
final class MiTiendaMapper {

    private MiTiendaMapper() {}

    static MiTienda aMiTienda(Tienda tienda) {
        return new MiTienda(
                tienda.getId(),
                tienda.getNombre(),
                tienda.getTelefono(),
                tienda.getCategoriaId(),
                tienda.getUbicacion().latitud(),
                tienda.getUbicacion().longitud(),
                tienda.getEstado().name(),
                tienda.estaAbierta(),
                tienda.getMotivoRechazo().orElse(null),
                tienda.getFechaRegistro(),
                tienda.getHorario().franjas().stream()
                        .map(franja -> new MiTienda.Franja(franja.dia().name(), franja.abre(), franja.cierra()))
                        .toList());
    }
}
