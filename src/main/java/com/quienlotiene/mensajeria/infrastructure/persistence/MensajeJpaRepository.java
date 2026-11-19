package com.quienlotiene.mensajeria.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface MensajeJpaRepository extends JpaRepository<MensajeEntity, UUID> {

    List<MensajeEntity> findByOrdenIdOrderByCreadoEnAscIdAsc(UUID ordenId);
}
