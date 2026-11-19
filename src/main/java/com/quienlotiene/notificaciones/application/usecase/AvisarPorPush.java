package com.quienlotiene.notificaciones.application.usecase;

import com.quienlotiene.notificaciones.domain.model.ResultadoPush;
import com.quienlotiene.notificaciones.domain.port.CanalTiempoReal;
import com.quienlotiene.notificaciones.domain.port.ServicioPush;
import com.quienlotiene.notificaciones.domain.port.SuscripcionPushRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * HU-031 (diagrama 28, pasos 7 a 14): si el usuario no tiene la app abierta (sin conexión SSE), le llega el aviso por
 * push a cada navegador suscrito. El push lleva solo el tipo y el id; el detalle lo pide la app al abrirse. Una
 * suscripción que el servicio da por vencida (404 o 410) se borra.
 */
@Slf4j
@Service
public class AvisarPorPush {

    private final CanalTiempoReal canal;
    private final ServicioPush servicioPush;
    private final SuscripcionPushRepository suscripcionPushRepository;

    AvisarPorPush(
            CanalTiempoReal canal, ServicioPush servicioPush, SuscripcionPushRepository suscripcionPushRepository) {
        this.canal = canal;
        this.servicioPush = servicioPush;
        this.suscripcionPushRepository = suscripcionPushRepository;
    }

    public void siNoEstaConectado(UUID usuarioId, String tipo, UUID recursoId) {
        if (usuarioId == null || !servicioPush.estaHabilitado() || canal.estaConectado(usuarioId)) {
            return;
        }
        var contenido = "{\"tipo\":\"" + tipo + "\",\"id\":\"" + recursoId + "\"}";
        for (var suscripcion : suscripcionPushRepository.listarPorUsuario(usuarioId)) {
            var resultado = servicioPush.enviar(suscripcion, contenido);
            if (resultado == ResultadoPush.VENCIDA) {
                suscripcionPushRepository.eliminar(suscripcion.id());
                log.info("Suscripción push {} vencida; se borra", suscripcion.id());
            }
        }
    }
}
