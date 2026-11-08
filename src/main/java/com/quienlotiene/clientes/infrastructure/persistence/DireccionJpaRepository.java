package com.quienlotiene.clientes.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface DireccionJpaRepository extends JpaRepository<DireccionEntity, UUID> {

    List<DireccionEntity> findByClienteIdOrderByCreadoEnAsc(UUID clienteId);

    long countByClienteId(UUID clienteId);
}
