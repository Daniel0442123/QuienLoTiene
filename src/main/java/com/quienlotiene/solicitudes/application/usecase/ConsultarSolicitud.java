package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.solicitudes.application.dto.SolicitudDelCliente;
import com.quienlotiene.solicitudes.domain.model.Respuesta;
import com.quienlotiene.solicitudes.domain.model.SolicitudNoEncontradaException;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-020 y HU-021: el cliente ve su solicitud, cuántas respuestas lleva y si venció o ya eligió. */
@Service
public class ConsultarSolicitud {

    private final SolicitudRepository solicitudRepository;
    private final EnvioSolicitudRepository envioRepository;
    private final RespuestaRepository respuestaRepository;

    ConsultarSolicitud(
            SolicitudRepository solicitudRepository,
            EnvioSolicitudRepository envioRepository,
            RespuestaRepository respuestaRepository) {
        this.solicitudRepository = solicitudRepository;
        this.envioRepository = envioRepository;
        this.respuestaRepository = respuestaRepository;
    }

    /**
     * @throws SolicitudNoEncontradaException si no existe o es de otro cliente (R-03)
     */
    @Transactional(readOnly = true)
    public SolicitudDelCliente ejecutar(UUID clienteId, UUID solicitudId) {
        var solicitud = solicitudRepository
                .buscarPorId(solicitudId)
                .filter(encontrada -> encontrada.esDe(clienteId))
                .orElseThrow(SolicitudNoEncontradaException::new);
        var respuestas = respuestaRepository.listarDe(solicitudId);
        var ordenId = respuestas.stream()
                .filter(Respuesta::fueElegida)
                .map(Respuesta::getOrdenId)
                .findFirst()
                .orElse(null);
        return new SolicitudDelCliente(
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
                solicitud.getFechaVencimiento(),
                envioRepository.listarDe(solicitudId).size(),
                respuestas.size(),
                ordenId);
    }
}
