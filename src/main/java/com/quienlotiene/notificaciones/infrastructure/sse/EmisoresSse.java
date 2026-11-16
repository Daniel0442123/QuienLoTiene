package com.quienlotiene.notificaciones.infrastructure.sse;

import com.quienlotiene.notificaciones.domain.model.AvisosRecientes;
import com.quienlotiene.notificaciones.domain.model.Notificacion;
import com.quienlotiene.notificaciones.domain.port.CanalTiempoReal;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Canal SSE (D-18, DECISIONES-BACKEND 12): una conexión por usuario, latido cada 25 segundos para que Caddy no corte
 * la conexión, y los avisos de los últimos 5 minutos en memoria para reenviarlos con {@code Last-Event-ID}. La API
 * corre en una sola instancia (diagrama 08), así que la memoria del proceso basta.
 */
@Slf4j
@Component
public class EmisoresSse implements CanalTiempoReal {

    /** Tras este tiempo el servidor cierra la conexión y el cliente reconecta solo (fetch-event-source). */
    static final Duration DURACION_CONEXION = Duration.ofMinutes(30);

    static final String COMENTARIO_LATIDO = "latido";

    private final Map<UUID, SseEmitter> emisoresPorUsuario = new ConcurrentHashMap<>();
    private final Map<UUID, AvisosRecientes> avisosPorUsuario = new ConcurrentHashMap<>();
    private final Clock reloj;

    EmisoresSse(Clock reloj) {
        this.reloj = reloj;
    }

    /**
     * Abre la conexión del usuario. Si ya tenía otra abierta, la cierra (una conexión por usuario) y reenvía los avisos
     * posteriores a {@code ultimoIdRecibido}.
     */
    public SseEmitter conectar(UUID usuarioId, String ultimoIdRecibido) {
        var emisor = new SseEmitter(DURACION_CONEXION.toMillis());
        var anterior = emisoresPorUsuario.put(usuarioId, emisor);
        if (anterior != null) {
            anterior.complete();
        }
        emisor.onCompletion(() -> emisoresPorUsuario.remove(usuarioId, emisor));
        emisor.onTimeout(() -> emisoresPorUsuario.remove(usuarioId, emisor));
        emisor.onError(falla -> emisoresPorUsuario.remove(usuarioId, emisor));

        // Primer mensaje: confirma la conexión enseguida (si no, algunos proxies esperan el primer byte)
        enviarA(usuarioId, emisor, SseEmitter.event().comment("conectado"));
        var perdidos = avisosDe(usuarioId).posterioresA(ultimoIdRecibido, reloj.instant());
        for (var aviso : perdidos) {
            enviarA(usuarioId, emisor, aEvento(aviso));
        }
        log.debug("Usuario {} conectado a eventos; {} avisos reenviados", usuarioId, perdidos.size());
        return emisor;
    }

    @Override
    public void enviar(Notificacion notificacion) {
        avisosDe(notificacion.usuarioId()).agregar(notificacion);
        var emisor = emisoresPorUsuario.get(notificacion.usuarioId());
        if (emisor != null) {
            enviarA(notificacion.usuarioId(), emisor, aEvento(notificacion));
        }
    }

    /** Cada 25 s: un comentario {@code :latido} que el cliente ignora. También limpia los avisos vencidos. */
    @Scheduled(fixedRateString = "${quienlotiene.notificaciones.intervalo-latido:PT25S}")
    void latir() {
        emisoresPorUsuario.forEach((usuarioId, emisor) ->
                enviarA(usuarioId, emisor, SseEmitter.event().comment(COMENTARIO_LATIDO)));
        var ahora = reloj.instant();
        avisosPorUsuario
                .entrySet()
                .removeIf(entrada -> !emisoresPorUsuario.containsKey(entrada.getKey())
                        && entrada.getValue().estaVacio(ahora));
    }

    boolean estaConectado(UUID usuarioId) {
        return emisoresPorUsuario.containsKey(usuarioId);
    }

    private AvisosRecientes avisosDe(UUID usuarioId) {
        return avisosPorUsuario.computeIfAbsent(usuarioId, sinAvisos -> new AvisosRecientes());
    }

    private static SseEmitter.SseEventBuilder aEvento(Notificacion notificacion) {
        return SseEmitter.event()
                .id(notificacion.id())
                .name(notificacion.tipo())
                .data(notificacion.datos(), MediaType.APPLICATION_JSON);
    }

    private void enviarA(UUID usuarioId, SseEmitter emisor, SseEmitter.SseEventBuilder evento) {
        try {
            emisor.send(evento);
        } catch (IOException | IllegalStateException conexionCerrada) {
            // El navegador cerró la pestaña o se cayó la red: el aviso queda en AvisosRecientes para el reenvío
            emisoresPorUsuario.remove(usuarioId, emisor);
            emisor.completeWithError(conexionCerrada);
        }
    }
}
