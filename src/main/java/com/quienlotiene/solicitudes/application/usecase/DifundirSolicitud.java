package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.solicitudes.api.SolicitudDifundidaEvent;
import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import com.quienlotiene.solicitudes.domain.model.Solicitud;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import com.quienlotiene.tiendas.api.TiendasApi;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * HU-017 Difundir a tiendas elegibles (diagrama 03 pasos 5, 6 y 10): pide a tiendas las APROBADAS, abiertas, en su
 * horario, de la categoría y dentro del radio; guarda a cuáles llegó y avisa con {@link SolicitudDifundidaEvent}.
 * Corre dentro de la transacción de quien publica la solicitud.
 */
@Slf4j
@Component
class DifundirSolicitud {

    private final TiendasApi tiendasApi;
    private final EnvioSolicitudRepository envioRepository;
    private final ApplicationEventPublisher publicadorEventos;

    DifundirSolicitud(
            TiendasApi tiendasApi,
            EnvioSolicitudRepository envioRepository,
            ApplicationEventPublisher publicadorEventos) {
        this.tiendasApi = tiendasApi;
        this.envioRepository = envioRepository;
        this.publicadorEventos = publicadorEventos;
    }

    /** @return cuántas tiendas recibieron la solicitud */
    int difundir(Solicitud solicitud, Instant ahora) {
        var elegibles = tiendasApi.buscarElegibles(
                solicitud.getCategoriaId(),
                solicitud.getUbicacion().latitud(),
                solicitud.getUbicacion().longitud(),
                solicitud.getRadioEnMetros(),
                ahora);
        envioRepository.guardarTodos(elegibles.stream()
                .map(tienda -> new EnvioSolicitud(
                        UUID.randomUUID(), solicitud.getId(), tienda.tiendaId(), tienda.distanciaEnMetros(), ahora))
                .toList());
        publicadorEventos.publishEvent(new SolicitudDifundidaEvent(
                solicitud.getId(),
                solicitud.getRadioEnMetros(),
                elegibles.stream()
                        .map(tienda -> new SolicitudDifundidaEvent.Destinatario(
                                tienda.tiendaId(), tienda.duenoId(), tienda.distanciaEnMetros()))
                        .toList()));
        log.info("Solicitud {} enviada a {} tiendas", solicitud.getId(), elegibles.size());
        return elegibles.size();
    }
}
