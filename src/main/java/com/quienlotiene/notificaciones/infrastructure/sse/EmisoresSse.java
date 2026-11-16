package com.quienlotiene.notificaciones.infrastructure.sse;

import com.quienlotiene.notificaciones.domain.model.AvisosRecientes;
import com.quienlotiene.notificaciones.domain.model.Notificacion;
import com.quienlotiene.notificaciones.domain.port.CanalTiempoReal;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Canal SSE (D-18, DECISIONES-BACKEND 12): hasta {@value #MAXIMO_CONEXIONES_POR_USUARIO} conexiones por usuario
 * (SC-014: el celular y el PC, o varias pestañas, abiertos a la vez), latido cada 25 segundos para que Caddy no corte
 * la conexión, y los avisos de los últimos 5 minutos en memoria para reenviarlos con {@code Last-Event-ID}. Cada aviso
 * llega a todas las conexiones del usuario. La API corre en una sola instancia (diagrama 08), así que la memoria del
 * proceso basta.
 */
@Slf4j
@Component
public class EmisoresSse implements CanalTiempoReal {

    /** Tras este tiempo el servidor cierra la conexión y el cliente reconecta solo (fetch-event-source). */
    static final Duration DURACION_CONEXION = Duration.ofMinutes(30);

    static final String COMENTARIO_LATIDO = "latido";

    /**
     * Tope de conexiones de un mismo usuario (SC-014). Al abrir una más, se cierra la más antigua: así una pestaña
     * olvidada o un cliente que reconecta en bucle no acumulan conexiones.
     */
    static final int MAXIMO_CONEXIONES_POR_USUARIO = 5;

    private final Map<UUID, List<SseEmitter>> emisoresPorUsuario = new ConcurrentHashMap<>();
    private final Map<UUID, AvisosRecientes> avisosPorUsuario = new ConcurrentHashMap<>();
    private final Clock reloj;

    EmisoresSse(Clock reloj) {
        this.reloj = reloj;
    }

    /**
     * Abre una conexión más del usuario y le reenvía los avisos posteriores a {@code ultimoIdRecibido}. Las otras
     * conexiones del mismo usuario siguen abiertas (SC-014); si ya tenía {@value #MAXIMO_CONEXIONES_POR_USUARIO}, se
     * cierra la más antigua.
     */
    public SseEmitter conectar(UUID usuarioId, String ultimoIdRecibido) {
        var emisor = new SseEmitter(DURACION_CONEXION.toMillis());
        var sobrantes = new ArrayList<SseEmitter>();
        emisoresPorUsuario.compute(usuarioId, (id, actuales) -> {
            var lista = actuales == null ? new CopyOnWriteArrayList<SseEmitter>() : actuales;
            lista.add(emisor);
            while (lista.size() > MAXIMO_CONEXIONES_POR_USUARIO) {
                sobrantes.add(lista.remove(0));
            }
            return lista;
        });
        sobrantes.forEach(SseEmitter::complete);
        emisor.onCompletion(() -> quitar(usuarioId, emisor));
        emisor.onTimeout(() -> quitar(usuarioId, emisor));
        emisor.onError(falla -> quitar(usuarioId, emisor));

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
        for (var emisor : emisoresPorUsuario.getOrDefault(notificacion.usuarioId(), List.of())) {
            enviarA(notificacion.usuarioId(), emisor, aEvento(notificacion));
        }
    }

    /** Cada 25 s: un comentario {@code :latido} que el cliente ignora. También limpia los avisos vencidos. */
    @Scheduled(fixedRateString = "${quienlotiene.notificaciones.intervalo-latido:PT25S}")
    void latir() {
        emisoresPorUsuario.forEach((usuarioId, emisores) -> emisores.forEach(
                emisor -> enviarA(usuarioId, emisor, SseEmitter.event().comment(COMENTARIO_LATIDO))));
        var ahora = reloj.instant();
        avisosPorUsuario
                .entrySet()
                .removeIf(entrada -> !emisoresPorUsuario.containsKey(entrada.getKey())
                        && entrada.getValue().estaVacio(ahora));
    }

    boolean estaConectado(UUID usuarioId) {
        return conexionesDe(usuarioId) > 0;
    }

    int conexionesDe(UUID usuarioId) {
        return emisoresPorUsuario.getOrDefault(usuarioId, List.of()).size();
    }

    /** Quita una conexión; si era la última del usuario, quita también su entrada. */
    private void quitar(UUID usuarioId, SseEmitter emisor) {
        emisoresPorUsuario.computeIfPresent(usuarioId, (id, lista) -> {
            lista.remove(emisor);
            return lista.isEmpty() ? null : lista;
        });
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
            quitar(usuarioId, emisor);
            emisor.completeWithError(conexionCerrada);
        }
    }
}
