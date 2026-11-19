package com.quienlotiene.reputacion.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ReporteJpaRepository extends JpaRepository<ReporteEntity, UUID> {

    List<ReporteEntity> findByEstadoOrderByCreadoEnAsc(String estado);

    @Query("""
            select reporte.tiendaId, count(reporte) from ReporteEntity reporte
            where reporte.tiendaId in :tiendaIds
            group by reporte.tiendaId
            """)
    List<Object[]> contarPorTienda(@Param("tiendaIds") Collection<UUID> tiendaIds);
}
