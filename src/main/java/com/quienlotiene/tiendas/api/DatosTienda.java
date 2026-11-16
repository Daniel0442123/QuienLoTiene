package com.quienlotiene.tiendas.api;

import java.util.UUID;

/**
 * Datos de una tienda que otros módulos necesitan para mostrar respuestas y órdenes. El teléfono solo se expone al
 * cliente con una orden activa (HU-032); quien lo recibe es responsable de no mostrarlo antes.
 */
public record DatosTienda(UUID tiendaId, UUID duenoId, String nombre, String telefono) {

    @Override
    public String toString() {
        return "DatosTienda[tiendaId=" + tiendaId + "]";
    }
}
