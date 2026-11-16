package com.quienlotiene.clientes.api;

import java.util.UUID;

/** Fachada para que ordenes lea la dirección de entrega que eligió el cliente (HU-024, diagrama 04 paso 1). */
public interface DireccionesApi {

    /**
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException CLI-003 si no existe o es de otro cliente
     */
    DireccionEntrega obtenerDireccionDe(UUID clienteId, UUID direccionId);
}
