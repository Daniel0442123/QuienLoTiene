package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.solicitudes.api.SolicitudVencidaEvent;
import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import java.time.Clock;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-021 Vencer y cerrar solicitudes (CU-29, diagrama 33): toma las ABIERTAS cuyo tiempo pasó, las deja VENCIDAS y
 * avisa al cliente y a las tiendas con {@link SolicitudVencidaEvent}. La llama la tarea programada cada minuto.
 */
@Slf4j
@Service
public class VencerSolicitudes {

    /** Cuántas solicitudes procesa cada ejecución; las demás quedan para el minuto siguiente. */
    static final int LOTE = 100;

    private final SolicitudRepository solicitudRepository;
    private final EnvioSolicitudRepository envioRepository;
    private final RespuestaRepository respuestaRepository;
    private final ApplicationEventPublisher publicadorEventos;
    private final Clock reloj;

    VencerSolicitudes(
            SolicitudRepository solicitudRepository,
            EnvioSolicitudRepository envioRepository,
            RespuestaRepository respuestaRepository,
            ApplicationEventPublisher publicadorEventos,
            Clock reloj) {
        this.solicitudRepository = solicitudRepository;
        this.envioRepository = envioRepository;
        this.respuestaRepository = respuestaRepository;
        this.publicadorEventos = publicadorEventos;
        this.reloj = reloj;
    }

    /** @return cuántas solicitudes vencieron en esta ejecución */
    @Transactional
    public int ejecutar() {
        var ahora = reloj.instant();
        var vencidas = 0;
        for (var solicitud : solicitudRepository.tomarVencidas(ahora, LOTE)) {
            if (!solicitud.vencer(ahora)) {
                continue;
            }
            solicitudRepository.guardarEstado(solicitud);
            var duenos = envioRepository.listarDe(solicitud.getId()).stream()
                    .map(EnvioSolicitud::duenoId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            publicadorEventos.publishEvent(new SolicitudVencidaEvent(
                    solicitud.getId(),
                    solicitud.getClienteId(),
                    respuestaRepository.listarDe(solicitud.getId()).size(),
                    duenos));
            log.info("Solicitud {} vencida sin elección", solicitud.getId());
            vencidas++;
        }
        return vencidas;
    }
}
