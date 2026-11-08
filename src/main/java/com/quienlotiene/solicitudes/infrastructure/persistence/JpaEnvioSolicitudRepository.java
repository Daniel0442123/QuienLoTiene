package com.quienlotiene.solicitudes.infrastructure.persistence;

import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
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
                    envio.id(), envio.solicitudId(), envio.tiendaId(), envio.distanciaEnMetros(), envio.fechaEnvio()));
        }
    }
}
