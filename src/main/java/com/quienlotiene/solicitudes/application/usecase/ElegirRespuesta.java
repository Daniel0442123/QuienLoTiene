package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.solicitudes.api.RespuestaElegida;
import com.quienlotiene.solicitudes.api.RespuestaElegidaEvent;
import com.quienlotiene.solicitudes.api.SolicitudesApi;
import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import com.quienlotiene.solicitudes.domain.model.RespuestaNoEncontradaException;
import com.quienlotiene.solicitudes.domain.model.TipoRespuesta;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-024 (parte de solicitudes, diagrama 04 paso 5): el cliente elige una respuesta, la solicitud queda RESUELTA y las
 * demás tiendas se enteran (HU-021, CP-SOL-021-02). Implementa {@link SolicitudesApi} para ordenes.
 */
@Slf4j
@Service
class ElegirRespuesta implements SolicitudesApi {

    private final SolicitudRepository solicitudRepository;
    private final RespuestaRepository respuestaRepository;
    private final EnvioSolicitudRepository envioRepository;
    private final ApplicationEventPublisher publicadorEventos;
    private final Clock reloj;

    ElegirRespuesta(
            SolicitudRepository solicitudRepository,
            RespuestaRepository respuestaRepository,
            EnvioSolicitudRepository envioRepository,
            ApplicationEventPublisher publicadorEventos,
            Clock reloj) {
        this.solicitudRepository = solicitudRepository;
        this.respuestaRepository = respuestaRepository;
        this.envioRepository = envioRepository;
        this.publicadorEventos = publicadorEventos;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public RespuestaElegida elegirRespuesta(UUID clienteId, UUID respuestaId, UUID ordenId) {
        var ahora = reloj.instant();
        var respuesta = respuestaRepository.buscarPorId(respuestaId).orElseThrow(RespuestaNoEncontradaException::new);
        var solicitud = solicitudRepository
                .buscarPorId(respuesta.getSolicitudId())
                .filter(encontrada -> encontrada.esDe(clienteId))
                .orElseThrow(RespuestaNoEncontradaException::new);

        respuesta.elegir(ordenId);
        solicitud.resolver(ahora);
        solicitudRepository.guardarEstado(solicitud);
        respuestaRepository.guardarEleccion(respuesta);

        var noElegidas = respuestaRepository.listarDe(solicitud.getId()).stream()
                .filter(otra ->
                        !otra.getId().equals(respuestaId) && otra.getTipo().ofreceProducto())
                .map(otra -> new RespuestaElegidaEvent.RespuestaNoElegida(otra.getId(), otra.getDuenoId()))
                .toList();
        var duenos = envioRepository.listarDe(solicitud.getId()).stream()
                .map(EnvioSolicitud::duenoId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        publicadorEventos.publishEvent(
                new RespuestaElegidaEvent(solicitud.getId(), respuestaId, ordenId, clienteId, duenos, noElegidas));
        log.info("Solicitud {} resuelta con la respuesta {}", solicitud.getId(), respuestaId);

        return new RespuestaElegida(
                solicitud.getId(),
                respuesta.getId(),
                respuesta.getTiendaId(),
                respuesta.getDuenoId(),
                respuesta.getTipo() == TipoRespuesta.PARECIDO
                        ? respuesta.getDescripcionAlternativa()
                        : solicitud.getProducto(),
                respuesta.getCantidadDisponible(),
                respuesta.getPrecioEnPesos(),
                respuesta.getCostoDomicilioEnPesos(),
                solicitud.esRestringida());
    }
}
