package com.quienlotiene.notificaciones.domain.port;

import com.quienlotiene.notificaciones.domain.model.ResultadoPush;
import com.quienlotiene.notificaciones.domain.model.SuscripcionPush;

/** Envía un aviso cifrado y firmado (VAPID) al servicio push del navegador (HU-031). */
public interface ServicioPush {

    /** {@code false} si el servidor no tiene claves VAPID: no se envía nada y la API sigue funcionando. */
    boolean estaHabilitado();

    /** Clave pública VAPID en Base64 URL, la que el navegador usa al suscribirse. */
    String clavePublica();

    ResultadoPush enviar(SuscripcionPush suscripcion, String contenidoJson);
}
