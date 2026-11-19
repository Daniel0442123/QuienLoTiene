package com.quienlotiene.mensajeria.domain.port;

import com.quienlotiene.mensajeria.domain.model.Mensaje;
import java.util.List;
import java.util.UUID;

/** Puerto de salida de los mensajes del chat (HU-033). */
public interface MensajeRepository {

    void guardar(Mensaje mensaje);

    /** Del más antiguo al más reciente. */
    List<Mensaje> listarPorOrden(UUID ordenId);
}
