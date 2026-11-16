package com.quienlotiene.solicitudes.infrastructure.persistence;

import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link EnvioSolicitudRepository}. */
@Component
class JpaEnvioSolicitudRepository implements EnvioSolicitudRepository {

    private final EntityManager entityManager;

    JpaEnvioSolicitudRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void guardarTodos(List<EnvioSolicitud> envios) {
        for (var envio : envios) {
            entityManager.persist(new EnvioSolicitudEntity(
                    envio.id(),
                    envio.solicitudId(),
                    envio.tiendaId(),
                    envio.duenoId(),
                    envio.distanciaEnMetros(),
                    envio.fechaEnvio()));
        }
    }

    @Override
    public Optional<EnvioSolicitud> buscar(UUID solicitudId, UUID tiendaId) {
        return entityManager
                .createQuery("""
                        select envio from EnvioSolicitudEntity envio
                        where envio.solicitudId = :solicitudId and envio.tiendaId = :tiendaId
                        """, EnvioSolicitudEntity.class)
                .setParameter("solicitudId", solicitudId)
                .setParameter("tiendaId", tiendaId)
                .getResultStream()
                .findFirst()
                .map(JpaEnvioSolicitudRepository::aDominio);
    }

    @Override
    public List<EnvioSolicitud> listarDe(UUID solicitudId) {
        return entityManager
                .createQuery(
                        "select envio from EnvioSolicitudEntity envio where envio.solicitudId = :solicitudId",
                        EnvioSolicitudEntity.class)
                .setParameter("solicitudId", solicitudId)
                .getResultStream()
                .map(JpaEnvioSolicitudRepository::aDominio)
                .toList();
    }

    private static EnvioSolicitud aDominio(EnvioSolicitudEntity fila) {
        return new EnvioSolicitud(
                fila.getId(),
                fila.getSolicitudId(),
                fila.getTiendaId(),
                fila.getDuenoId(),
                fila.getDistanciaMetros(),
                fila.getCreadoEn());
    }
}
