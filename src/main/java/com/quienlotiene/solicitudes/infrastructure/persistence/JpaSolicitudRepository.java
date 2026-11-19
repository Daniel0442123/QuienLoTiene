package com.quienlotiene.solicitudes.infrastructure.persistence;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.solicitudes.domain.model.EstadoSolicitud;
import com.quienlotiene.solicitudes.domain.model.Modalidad;
import com.quienlotiene.solicitudes.domain.model.Solicitud;
import com.quienlotiene.solicitudes.domain.model.SolicitudParaTienda;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link SolicitudRepository}. */
@Component
class JpaSolicitudRepository implements SolicitudRepository {

    private final SolicitudJpaRepository solicitudJpaRepository;
    private final EntityManager entityManager;
    private final Clock reloj;

    JpaSolicitudRepository(SolicitudJpaRepository solicitudJpaRepository, EntityManager entityManager, Clock reloj) {
        this.solicitudJpaRepository = solicitudJpaRepository;
        this.entityManager = entityManager;
        this.reloj = reloj;
    }

    @Override
    public void guardarNueva(Solicitud solicitud) {
        entityManager.persist(new SolicitudEntity(
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
                solicitud.esRestringida(),
                solicitud.getEstado().name(),
                solicitud.getFechaVencimiento(),
                solicitud.getFechaPublicacion()));
    }

    @Override
    public List<Instant> listarFechasPublicacionDesde(UUID clienteId, Instant desde) {
        return solicitudJpaRepository.listarFechasPublicacionDesde(clienteId, desde);
    }

    @Override
    public Optional<Solicitud> buscarPorId(UUID solicitudId) {
        return solicitudJpaRepository.findById(solicitudId).map(JpaSolicitudRepository::aDominio);
    }

    @Override
    public void guardarEstado(Solicitud solicitud) {
        var fila = solicitudJpaRepository.findById(solicitud.getId()).orElseThrow();
        fila.cambiarEstado(solicitud.getEstado().name(), reloj.instant());
    }

    @Override
    public List<SolicitudParaTienda> listarDisponiblesParaTienda(UUID tiendaId, Instant ahora) {
        return solicitudJpaRepository.listarDisponiblesParaTienda(tiendaId, ahora).stream()
                .map(fila -> new SolicitudParaTienda(aDominio((SolicitudEntity) fila[0]), (Integer) fila[1]))
                .toList();
    }

    @Override
    public List<Solicitud> tomarVencidas(Instant ahora, int limite) {
        return solicitudJpaRepository.tomarVencidas(ahora, limite).stream()
                .map(JpaSolicitudRepository::aDominio)
                .toList();
    }

    private static Solicitud aDominio(SolicitudEntity fila) {
        return Solicitud.reconstituir(
                fila.getId(),
                fila.getClienteId(),
                fila.getCategoriaId(),
                fila.getProducto(),
                fila.getCantidad(),
                Modalidad.valueOf(fila.getModalidad()),
                fila.isUrgente(),
                new Ubicacion(fila.latitud(), fila.longitud()),
                fila.getRadioMetros(),
                fila.isRestringida(),
                EstadoSolicitud.valueOf(fila.getEstado()),
                fila.getCreadoEn(),
                fila.getVenceEn());
    }
}
