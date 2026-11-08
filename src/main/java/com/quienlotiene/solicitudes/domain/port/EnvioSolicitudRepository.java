package com.quienlotiene.solicitudes.domain.port;

import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import java.util.List;

/** Guarda a qué tiendas llegó cada solicitud ({@code solicitudes.envio_solicitud}). */
public interface EnvioSolicitudRepository {

    void guardarTodos(List<EnvioSolicitud> envios);
}
