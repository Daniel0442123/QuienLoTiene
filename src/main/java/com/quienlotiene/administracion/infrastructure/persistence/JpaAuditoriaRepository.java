package com.quienlotiene.administracion.infrastructure.persistence;

import com.quienlotiene.administracion.domain.model.RegistroAuditoria;
import com.quienlotiene.administracion.domain.port.AuditoriaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link AuditoriaRepository}. */
@Component
class JpaAuditoriaRepository implements AuditoriaRepository {

    private final EntityManager entityManager;

    JpaAuditoriaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void registrar(RegistroAuditoria registro) {
        entityManager.persist(new AuditoriaEntity(
                registro.id(),
                registro.administradorId(),
                registro.accion().name(),
                registro.recursoId(),
                registro.detalle(),
                registro.fecha()));
    }
}
