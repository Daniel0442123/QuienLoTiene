package com.quienlotiene.solicitudes.infrastructure.persistence;

import com.quienlotiene.solicitudes.domain.model.Solicitud;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link SolicitudRepository}. */
@Component
class JpaSolicitudRepository implements SolicitudRepository {

    private final SolicitudJpaRepository solicitudJpaRepository;
    private final EntityManager entityManager;

    JpaSolicitudRepository(SolicitudJpaRepository solicitudJpaRepository, EntityManager entityManager) {
        this.solicitudJpaRepository = solicitudJpaRepository;
        this.entityManager = entityManager;
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
                solicitud.getEstado().name(),
                solicitud.getFechaVencimiento(),
                solicitud.getFechaPublicacion()));
    }

    @Override
    public List<Instant> listarFechasPublicacionDesde(UUID clienteId, Instant desde) {
        return solicitudJpaRepository.listarFechasPublicacionDesde(clienteId, desde);
    }
}
