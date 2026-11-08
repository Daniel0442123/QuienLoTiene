package com.quienlotiene.tiendas.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TiendaJpaRepository extends JpaRepository<TiendaEntity, UUID> {

    List<TiendaEntity> findByEstadoOrderByCreadoEnAsc(String estado);
}
