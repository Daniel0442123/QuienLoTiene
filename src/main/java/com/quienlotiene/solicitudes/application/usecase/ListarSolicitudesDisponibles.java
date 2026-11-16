package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.solicitudes.application.dto.SolicitudDisponible;
import com.quienlotiene.solicitudes.domain.model.SolicitudParaTienda;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import com.quienlotiene.tiendas.api.DatosTiendasApi;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-018 Ver solicitudes nuevas (CU-20, diagrama 26): las ABIERTAS que llegaron a la tienda y que todavía no responde,
 * urgentes primero, sin datos del cliente y con la distancia redondeada a 100 m.
 */
@Service
public class ListarSolicitudesDisponibles {

    private final DatosTiendasApi datosTiendasApi;
    private final SolicitudRepository solicitudRepository;
    private final RespuestaRepository respuestaRepository;
    private final Clock reloj;

    ListarSolicitudesDisponibles(
            DatosTiendasApi datosTiendasApi,
            SolicitudRepository solicitudRepository,
            RespuestaRepository respuestaRepository,
            Clock reloj) {
        this.datosTiendasApi = datosTiendasApi;
        this.solicitudRepository = solicitudRepository;
        this.respuestaRepository = respuestaRepository;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<SolicitudDisponible> ejecutar(UUID duenoId) {
        var tienda = datosTiendasApi.obtenerTiendaDelDueno(duenoId);
        return solicitudRepository.listarDisponiblesParaTienda(tienda.tiendaId(), reloj.instant()).stream()
                .map(disponible -> aTarjeta(disponible, tienda.tiendaId()))
                .toList();
    }

    private SolicitudDisponible aTarjeta(SolicitudParaTienda disponible, UUID tiendaId) {
        var solicitud = disponible.solicitud();
        return new SolicitudDisponible(
                solicitud.getId(),
                solicitud.getCategoriaId(),
                solicitud.getProducto(),
                solicitud.getCantidad(),
                solicitud.getModalidad().name(),
                solicitud.esUrgente(),
                disponible.distanciaAproximadaEnMetros(),
                solicitud.getFechaPublicacion(),
                solicitud.getFechaVencimiento(),
                respuestaRepository
                        .buscarUltimoPrecio(tiendaId, solicitud.getProducto())
                        .orElse(null));
    }
}
