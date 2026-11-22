package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.solicitudes.api.RadioAmpliadoEvent;
import com.quienlotiene.solicitudes.domain.port.ParametrosSolicitudes;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import java.time.Clock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-022 Ampliar radio automáticamente (CU-31, diagrama 35): toma las solicitudes ABIERTAS que llevan el tiempo
 * configurado sin ninguna respuesta, amplía su radio una sola vez, las envía a las tiendas que quedaron dentro y le
 * avisa al cliente con {@link RadioAmpliadoEvent}. La llama la tarea programada cada minuto.
 */
@Slf4j
@Service
public class AmpliarRadioSolicitudes {

    /** Cuántas solicitudes procesa cada ejecución; las demás quedan para el minuto siguiente. */
    static final int LOTE = 100;

    private final SolicitudRepository solicitudRepository;
    private final DifundirSolicitud difundirSolicitud;
    private final ParametrosSolicitudes parametros;
    private final ApplicationEventPublisher publicadorEventos;
    private final Clock reloj;

    AmpliarRadioSolicitudes(
            SolicitudRepository solicitudRepository,
            DifundirSolicitud difundirSolicitud,
            ParametrosSolicitudes parametros,
            ApplicationEventPublisher publicadorEventos,
            Clock reloj) {
        this.solicitudRepository = solicitudRepository;
        this.difundirSolicitud = difundirSolicitud;
        this.parametros = parametros;
        this.publicadorEventos = publicadorEventos;
        this.reloj = reloj;
    }

    /** @return cuántas solicitudes ampliaron su radio en esta ejecución */
    @Transactional
    public int ejecutar() {
        var ahora = reloj.instant();
        var publicadasHasta = ahora.minus(parametros.esperaParaAmpliar());
        var ampliadas = 0;
        for (var solicitud : solicitudRepository.tomarParaAmpliar(publicadasHasta, ahora, LOTE)) {
            if (!solicitud.ampliarRadio(parametros.radioAmpliadoEnMetros(), ahora)) {
                continue;
            }
            solicitudRepository.guardarRadio(solicitud);
            var tiendasNuevas = difundirSolicitud.difundirATiendasNuevas(solicitud, ahora);
            publicadorEventos.publishEvent(new RadioAmpliadoEvent(
                    solicitud.getId(), solicitud.getClienteId(), solicitud.getRadioEnMetros(), tiendasNuevas));
            log.info(
                    "Solicitud {} ampliada a {} m; {} tiendas nuevas",
                    solicitud.getId(),
                    solicitud.getRadioEnMetros(),
                    tiendasNuevas);
            ampliadas++;
        }
        return ampliadas;
    }
}
