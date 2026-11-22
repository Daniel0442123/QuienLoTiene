package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.solicitudes.api.SolicitudDifundidaEvent;
import com.quienlotiene.solicitudes.domain.model.DistanciaAproximada;
import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import com.quienlotiene.solicitudes.domain.model.Solicitud;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import com.quienlotiene.tiendas.api.TiendaElegible;
import com.quienlotiene.tiendas.api.TiendasApi;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * HU-017 Difundir a tiendas elegibles (diagrama 03 pasos 5, 6 y 10): pide a tiendas las APROBADAS, abiertas, en su
 * horario, de la categoría y dentro del radio; guarda a cuáles llegó y avisa con {@link SolicitudDifundidaEvent}.
 * Corre dentro de la transacción de quien publica la solicitud o de quien amplía su radio (HU-022, CU-31).
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
        return enviarA(solicitud, buscarElegibles(solicitud, ahora), ahora);
    }

    /**
     * HU-022 (diagrama 35 pasos 5 y 6): con el radio ya ampliado, envía la solicitud solo a las tiendas elegibles que
     * todavía no la habían recibido.
     *
     * @return cuántas tiendas nuevas la recibieron
     */
    int difundirATiendasNuevas(Solicitud solicitud, Instant ahora) {
        var yaLaRecibieron = new HashSet<UUID>();
        envioRepository.listarDe(solicitud.getId()).forEach(envio -> yaLaRecibieron.add(envio.tiendaId()));
        var nuevas = buscarElegibles(solicitud, ahora).stream()
                .filter(tienda -> !yaLaRecibieron.contains(tienda.tiendaId()))
                .toList();
        return nuevas.isEmpty() ? 0 : enviarA(solicitud, nuevas, ahora);
    }

    private List<TiendaElegible> buscarElegibles(Solicitud solicitud, Instant ahora) {
        return tiendasApi.buscarElegibles(
                solicitud.getCategoriaId(),
                solicitud.getUbicacion().latitud(),
                solicitud.getUbicacion().longitud(),
                solicitud.getRadioEnMetros(),
                ahora);
    }

    private int enviarA(Solicitud solicitud, List<TiendaElegible> elegibles, Instant ahora) {
        envioRepository.guardarTodos(elegibles.stream()
                .map(tienda -> new EnvioSolicitud(
                        UUID.randomUUID(),
                        solicitud.getId(),
                        tienda.tiendaId(),
                        tienda.duenoId(),
                        tienda.distanciaEnMetros(),
                        ahora))
                .toList());
        publicadorEventos.publishEvent(new SolicitudDifundidaEvent(
                solicitud.getId(),
                solicitud.getProducto(),
                solicitud.getCantidad(),
                solicitud.getModalidad().name(),
                solicitud.esUrgente(),
                solicitud.getFechaVencimiento(),
                solicitud.getRadioEnMetros(),
                elegibles.stream()
                        .map(tienda -> new SolicitudDifundidaEvent.Destinatario(
                                tienda.tiendaId(),
                                tienda.duenoId(),
                                DistanciaAproximada.redondear(tienda.distanciaEnMetros())))
                        .toList()));
        log.info("Solicitud {} enviada a {} tiendas", solicitud.getId(), elegibles.size());
        return elegibles.size();
    }
}
