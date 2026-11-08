package com.quienlotiene.tiendas.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface DocumentoVerificacionJpaRepository extends JpaRepository<DocumentoVerificacionEntity, UUID> {

    List<DocumentoVerificacionEntity> findByTiendaIdOrderByTipoAsc(UUID tiendaId);
}
