package com.quienlotiene.notificaciones.application.usecase;

import com.quienlotiene.notificaciones.application.dto.ConfiguracionPush;
import com.quienlotiene.notificaciones.domain.model.SuscripcionPush;
import com.quienlotiene.notificaciones.domain.port.ServicioPush;
import com.quienlotiene.notificaciones.domain.port.SuscripcionPushRepository;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-031 (CU-24): el navegador de la tienda se suscribe a los avisos push con la clave pública VAPID y puede darse de
 * baja. Si la tienda rechaza el permiso del navegador, nunca llega aquí y no recibe push (CP-NOT-031-02).
 */
@Service
public class GestionarSuscripcionesPush {

    private final SuscripcionPushRepository suscripcionPushRepository;
    private final ServicioPush servicioPush;
    private final Clock reloj;

    GestionarSuscripcionesPush(
            SuscripcionPushRepository suscripcionPushRepository, ServicioPush servicioPush, Clock reloj) {
        this.suscripcionPushRepository = suscripcionPushRepository;
        this.servicioPush = servicioPush;
        this.reloj = reloj;
    }

    public ConfiguracionPush configuracion() {
        return servicioPush.estaHabilitado()
                ? new ConfiguracionPush(true, servicioPush.clavePublica())
                : new ConfiguracionPush(false, null);
    }

    /**
     * @throws com.quienlotiene.notificaciones.domain.model.SuscripcionPushInvalidaException si el endpoint o las claves
     *     no sirven
     */
    @Transactional
    public void suscribir(UUID usuarioId, String endpoint, String p256dh, String auth) {
        suscripcionPushRepository.guardar(
                SuscripcionPush.suscribir(UUID.randomUUID(), usuarioId, endpoint, p256dh, auth, reloj.instant()));
    }

    /** Darse de baja es idempotente: si la suscripción no existe o es de otro usuario, no pasa nada. */
    @Transactional
    public void cancelar(UUID usuarioId, String endpoint) {
        suscripcionPushRepository.eliminar(usuarioId, endpoint);
    }
}
