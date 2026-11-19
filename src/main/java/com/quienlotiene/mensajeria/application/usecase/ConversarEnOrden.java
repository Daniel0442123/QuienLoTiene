package com.quienlotiene.mensajeria.application.usecase;

import com.quienlotiene.mensajeria.api.MensajeEnviadoEvent;
import com.quienlotiene.mensajeria.application.dto.ChatDeOrden;
import com.quienlotiene.mensajeria.application.dto.MensajeVista;
import com.quienlotiene.mensajeria.domain.model.ChatSoloLecturaException;
import com.quienlotiene.mensajeria.domain.model.Mensaje;
import com.quienlotiene.mensajeria.domain.port.MensajeRepository;
import com.quienlotiene.ordenes.api.OrdenesApi;
import java.time.Clock;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-033 Mini chat (CU-12): solo el cliente y la tienda de la orden leen y escriben (una orden ajena responde el 404
 * de ordenes, R-03). Con la orden en un estado final el chat queda en solo lectura (CP-MEN-033-02). Cada mensaje nuevo
 * se avisa a la otra parte por SSE (CP-MEN-033-01).
 */
@Service
public class ConversarEnOrden {

    private final OrdenesApi ordenesApi;
    private final MensajeRepository mensajeRepository;
    private final ApplicationEventPublisher publicadorEventos;
    private final Clock reloj;

    ConversarEnOrden(
            OrdenesApi ordenesApi,
            MensajeRepository mensajeRepository,
            ApplicationEventPublisher publicadorEventos,
            Clock reloj) {
        this.ordenesApi = ordenesApi;
        this.mensajeRepository = mensajeRepository;
        this.publicadorEventos = publicadorEventos;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public ChatDeOrden listar(UUID usuarioId, UUID ordenId) {
        var orden = ordenesApi.obtenerResumen(ordenId, usuarioId);
        var mensajes = mensajeRepository.listarPorOrden(ordenId).stream()
                .map(mensaje -> aVista(mensaje, usuarioId))
                .toList();
        return new ChatDeOrden(ordenId, !orden.esActiva(), mensajes);
    }

    /**
     * @throws ChatSoloLecturaException si la orden ya no está activa
     */
    @Transactional
    public MensajeVista enviar(UUID usuarioId, UUID ordenId, String texto) {
        var orden = ordenesApi.obtenerResumen(ordenId, usuarioId);
        if (!orden.esActiva()) {
            throw new ChatSoloLecturaException();
        }
        var mensaje = Mensaje.escribir(
                UUID.randomUUID(), ordenId, usuarioId, orden.esDelCliente(usuarioId), texto, reloj.instant());
        mensajeRepository.guardar(mensaje);
        publicadorEventos.publishEvent(new MensajeEnviadoEvent(ordenId, mensaje.id(), orden.otraParte(usuarioId)));
        return aVista(mensaje, usuarioId);
    }

    private static MensajeVista aVista(Mensaje mensaje, UUID usuarioId) {
        return new MensajeVista(
                mensaje.id(),
                mensaje.autorEsCliente() ? "CLIENTE" : "TIENDA",
                mensaje.autorId().equals(usuarioId),
                mensaje.texto(),
                mensaje.fecha());
    }
}
