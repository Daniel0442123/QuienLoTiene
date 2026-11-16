package com.quienlotiene.ordenes.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface HistorialEstadoJpaRepository extends JpaRepository<HistorialEstadoEntity, UUID> {

    List<HistorialEstadoEntity> findByOrdenIdOrderByCambiadoEnAsc(UUID ordenId);

    List<HistorialEstadoEntity> findByOrdenIdInOrderByCambiadoEnAsc(Collection<UUID> ordenIds);

    long countByOrdenId(UUID ordenId);
}
