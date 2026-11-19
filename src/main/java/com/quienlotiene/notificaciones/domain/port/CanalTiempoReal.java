package com.quienlotiene.notificaciones.domain.port;

import com.quienlotiene.notificaciones.domain.model.Notificacion;
import java.util.UUID;

/** Entrega avisos al usuario conectado (SSE). Si no está conectado, el aviso queda para cuando reconecte. */
public interface CanalTiempoReal {

    void enviar(Notificacion notificacion);

    /** {@code true} si el usuario tiene al menos una conexión abierta (HU-031: entonces no hace falta push). */
    boolean estaConectado(UUID usuarioId);
}
