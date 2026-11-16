package com.quienlotiene.ordenes.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OrdenJpaRepository extends JpaRepository<OrdenEntity, UUID> {

    @Query("""
            select orden from OrdenEntity orden
            where orden.clienteId = :usuarioId or orden.duenoTiendaId = :usuarioId
            order by orden.creadoEn desc
            """)
    List<OrdenEntity> listarDeUsuario(@Param("usuarioId") UUID usuarioId, Limit limite);
}
