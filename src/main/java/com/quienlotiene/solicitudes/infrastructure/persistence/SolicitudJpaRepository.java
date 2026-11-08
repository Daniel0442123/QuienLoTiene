package com.quienlotiene.solicitudes.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SolicitudJpaRepository extends JpaRepository<SolicitudEntity, UUID> {

    @Query("""
            select solicitud.creadoEn from SolicitudEntity solicitud
            where solicitud.clienteId = :clienteId and solicitud.creadoEn > :desde
            """)
    List<Instant> listarFechasPublicacionDesde(@Param("clienteId") UUID clienteId, @Param("desde") Instant desde);
}
