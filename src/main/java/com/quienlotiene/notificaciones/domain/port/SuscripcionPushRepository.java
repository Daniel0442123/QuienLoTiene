package com.quienlotiene.notificaciones.domain.port;

import com.quienlotiene.notificaciones.domain.model.SuscripcionPush;
import java.util.List;
import java.util.UUID;

/** Puerto de salida de las suscripciones Web Push (HU-031). */
public interface SuscripcionPushRepository {

    /** Guarda la suscripción; si el endpoint ya estaba (de este u otro usuario), lo reemplaza. */
    void guardar(SuscripcionPush suscripcion);

    void eliminar(UUID usuarioId, String endpoint);

    void eliminar(UUID suscripcionId);

    List<SuscripcionPush> listarPorUsuario(UUID usuarioId);
}
