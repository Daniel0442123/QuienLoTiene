package com.quienlotiene.solicitudes.domain.port;

import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Guarda a qué tiendas llegó cada solicitud ({@code solicitudes.envio_solicitud}). */
public interface EnvioSolicitudRepository {

    void guardarTodos(List<EnvioSolicitud> envios);

    /** El envío de la solicitud a esa tienda, si le llegó. */
    Optional<EnvioSolicitud> buscar(UUID solicitudId, UUID tiendaId);

    List<EnvioSolicitud> listarDe(UUID solicitudId);
}
