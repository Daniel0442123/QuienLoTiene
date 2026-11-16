package com.quienlotiene.notificaciones.domain.port;

import com.quienlotiene.notificaciones.domain.model.Notificacion;

/** Entrega avisos al usuario conectado (SSE). Si no está conectado, el aviso queda para cuando reconecte. */
public interface CanalTiempoReal {

    void enviar(Notificacion notificacion);
}
