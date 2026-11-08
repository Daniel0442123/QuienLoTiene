package com.quienlotiene.identidad.infrastructure.persistence;

import com.quienlotiene.identidad.domain.model.AutorizacionDatos;
import com.quienlotiene.identidad.domain.port.AutorizacionDatosRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link AutorizacionDatosRepository}. */
@Component
class JpaAutorizacionDatosRepository implements AutorizacionDatosRepository {

    private final EntityManager entityManager;
    private final AutorizacionDatosPersistenceMapper autorizacionMapper;

    JpaAutorizacionDatosRepository(EntityManager entityManager, AutorizacionDatosPersistenceMapper autorizacionMapper) {
        this.entityManager = entityManager;
        this.autorizacionMapper = autorizacionMapper;
    }

    @Override
    public void guardar(AutorizacionDatos autorizacion) {
        // Las autorizaciones nunca se editan: siempre es una fila nueva
        entityManager.persist(autorizacionMapper.aEntity(autorizacion));
    }
}
