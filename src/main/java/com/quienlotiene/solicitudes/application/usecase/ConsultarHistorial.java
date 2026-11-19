package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.solicitudes.application.dto.HistorialSolicitudes;
import com.quienlotiene.solicitudes.application.dto.PublicarSolicitudComando;
import com.quienlotiene.solicitudes.application.dto.SolicitudDelHistorial;
import com.quienlotiene.solicitudes.application.dto.SolicitudPublicada;
import com.quienlotiene.solicitudes.domain.model.EstadoSolicitud;
import com.quienlotiene.solicitudes.domain.model.Solicitud;
import com.quienlotiene.solicitudes.domain.model.SolicitudIncompletaException;
import com.quienlotiene.solicitudes.domain.model.SolicitudNoEncontradaException;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-008 Consultar historial y repetir (RF-008) y SC-018 «Mis preguntas»: el cliente ve sus solicitudes de la más
 * reciente a la más antigua, con cuántas respuestas tiene cada una, y puede repetir una con la ubicación de ahora. La
 * repetición pasa por {@link PublicarSolicitud}, así que cuenta para el límite de 10 por hora y vuelve a revisar si el
 * producto es restringido.
 */
@Service
public class ConsultarHistorial {

    static final int TAMANO_MAXIMO = 50;

    private final SolicitudRepository solicitudRepository;
    private final RespuestaRepository respuestaRepository;
    private final PublicarSolicitud publicarSolicitud;
    private final Clock reloj;

    ConsultarHistorial(
            SolicitudRepository solicitudRepository,
            RespuestaRepository respuestaRepository,
            PublicarSolicitud publicarSolicitud,
            Clock reloj) {
        this.solicitudRepository = solicitudRepository;
        this.respuestaRepository = respuestaRepository;
        this.publicarSolicitud = publicarSolicitud;
        this.reloj = reloj;
    }

    /**
     * @throws SolicitudIncompletaException si la página es negativa o el tamaño no está entre 1 y 50
     */
    @Transactional(readOnly = true)
    public HistorialSolicitudes listar(UUID clienteId, int pagina, int tamano) {
        if (pagina < 0 || tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new SolicitudIncompletaException("la página debe ser 0 o más y el tamaño de 1 a " + TAMANO_MAXIMO);
        }
        // Una de más para saber si hay página siguiente sin contar todas
        var encontradas = solicitudRepository.listarDelCliente(clienteId, pagina, tamano + 1);
        var hayMas = encontradas.size() > tamano;
        var solicitudes = hayMas ? encontradas.subList(0, tamano) : encontradas;
        var ids = solicitudes.stream().map(Solicitud::getId).toList();
        var respuestas = respuestaRepository.contarPorSolicitud(ids);
        var ordenes = respuestaRepository.ordenesPorSolicitud(ids);
        var ahora = reloj.instant();
        var vistas = solicitudes.stream()
                .map(solicitud -> new SolicitudDelHistorial(
                        solicitud.getId(),
                        solicitud.getCategoriaId(),
                        solicitud.getProducto(),
                        solicitud.getCantidad(),
                        solicitud.getModalidad().name(),
                        solicitud.esUrgente(),
                        solicitud.getEstado().name(),
                        solicitud.getEstado() == EstadoSolicitud.ABIERTA
                                && solicitud.getFechaVencimiento().isAfter(ahora),
                        solicitud.getFechaPublicacion(),
                        solicitud.getFechaVencimiento(),
                        respuestas.getOrDefault(solicitud.getId(), 0),
                        ordenes.get(solicitud.getId()),
                        solicitud.esRestringida()))
                .toList();
        return new HistorialSolicitudes(vistas, pagina, hayMas);
    }

    /**
     * CP-CLI-008-01: publica una solicitud nueva con el mismo producto, cantidad, categoría, modalidad y urgencia.
     *
     * @throws SolicitudNoEncontradaException si no existe o es de otro cliente (R-03)
     */
    @Transactional
    public SolicitudPublicada repetir(UUID clienteId, UUID solicitudId, double latitud, double longitud) {
        var original = solicitudRepository
                .buscarPorId(solicitudId)
                .filter(encontrada -> encontrada.esDe(clienteId))
                .orElseThrow(SolicitudNoEncontradaException::new);
        return publicarSolicitud.ejecutar(new PublicarSolicitudComando(
                clienteId,
                original.getCategoriaId(),
                original.getProducto(),
                original.getCantidad(),
                original.getModalidad().name(),
                original.esUrgente(),
                latitud,
                longitud));
    }
}
