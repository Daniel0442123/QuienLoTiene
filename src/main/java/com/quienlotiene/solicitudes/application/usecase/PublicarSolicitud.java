package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.solicitudes.api.SolicitudPublicadaEvent;
import com.quienlotiene.solicitudes.application.dto.PublicarSolicitudComando;
import com.quienlotiene.solicitudes.application.dto.SolicitudPublicada;
import com.quienlotiene.solicitudes.domain.model.LimitePublicacion;
import com.quienlotiene.solicitudes.domain.model.Modalidad;
import com.quienlotiene.solicitudes.domain.model.Solicitud;
import com.quienlotiene.solicitudes.domain.model.SolicitudIncompletaException;
import com.quienlotiene.solicitudes.domain.port.ParametrosSolicitudes;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-016 Publicar solicitud (CU-04, diagrama 03 pasos 1 a 8): revisa el límite de 10 por hora, guarda la solicitud
 * ABIERTA con su vencimiento a 30 minutos y publica {@link SolicitudPublicadaEvent} para la difusión (HU-017).
 */
@Slf4j
@Service
public class PublicarSolicitud {

    private final SolicitudRepository solicitudRepository;
    private final ParametrosSolicitudes parametros;
    private final ApplicationEventPublisher publicadorEventos;
    private final Clock reloj;

    PublicarSolicitud(
            SolicitudRepository solicitudRepository,
            ParametrosSolicitudes parametros,
            ApplicationEventPublisher publicadorEventos,
            Clock reloj) {
        this.solicitudRepository = solicitudRepository;
        this.parametros = parametros;
        this.publicadorEventos = publicadorEventos;
        this.reloj = reloj;
    }

    @Transactional
    public SolicitudPublicada ejecutar(PublicarSolicitudComando comando) {
        var ahora = reloj.instant();
        var fechasEnLaUltimaHora = solicitudRepository.listarFechasPublicacionDesde(
                comando.clienteId(), LimitePublicacion.inicioDeVentana(ahora));
        LimitePublicacion.verificar(fechasEnLaUltimaHora, parametros.maximoPorHora(), ahora);

        var solicitud = Solicitud.publicar(
                UUID.randomUUID(),
                comando.clienteId(),
                comando.categoriaId(),
                comando.producto(),
                comando.cantidad(),
                modalidadDe(comando.modalidad()),
                comando.esUrgente(),
                new Ubicacion(comando.latitud(), comando.longitud()),
                parametros.radioInicialEnMetros(),
                parametros.vigencia(),
                ahora);
        solicitudRepository.guardarNueva(solicitud);
        publicadorEventos.publishEvent(aEvento(solicitud));
        log.info("Solicitud {} publicada por el cliente {}", solicitud.getId(), solicitud.getClienteId());

        return aResultado(solicitud);
    }

    /** {@code null} se deja pasar: el dominio responde que falta la modalidad. */
    private static Modalidad modalidadDe(String modalidad) {
        if (modalidad == null) {
            return null;
        }
        try {
            return Modalidad.valueOf(modalidad);
        } catch (IllegalArgumentException modalidadDesconocida) {
            throw new SolicitudIncompletaException("la modalidad debe ser RECOGER o DOMICILIO");
        }
    }

    private static SolicitudPublicadaEvent aEvento(Solicitud solicitud) {
        return new SolicitudPublicadaEvent(
                solicitud.getId(),
                solicitud.getClienteId(),
                solicitud.getCategoriaId(),
                solicitud.getProducto(),
                solicitud.getCantidad(),
                solicitud.getModalidad().name(),
                solicitud.esUrgente(),
                solicitud.getUbicacion().latitud(),
                solicitud.getUbicacion().longitud(),
                solicitud.getRadioEnMetros(),
                solicitud.getFechaVencimiento());
    }

    private static SolicitudPublicada aResultado(Solicitud solicitud) {
        return new SolicitudPublicada(
                solicitud.getId(),
                solicitud.getCategoriaId(),
                solicitud.getProducto(),
                solicitud.getCantidad(),
                solicitud.getModalidad().name(),
                solicitud.esUrgente(),
                solicitud.getUbicacion().latitud(),
                solicitud.getUbicacion().longitud(),
                solicitud.getRadioEnMetros(),
                solicitud.getEstado().name(),
                solicitud.getFechaPublicacion(),
                solicitud.getFechaVencimiento());
    }
}
