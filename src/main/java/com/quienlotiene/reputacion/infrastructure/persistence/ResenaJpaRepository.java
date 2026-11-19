package com.quienlotiene.reputacion.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ResenaJpaRepository extends JpaRepository<ResenaEntity, UUID> {

    boolean existsByOrdenId(UUID ordenId);

    Optional<ResenaEntity> findByOrdenId(UUID ordenId);

    List<ResenaEntity> findByTiendaIdOrderByCreadoEnDesc(UUID tiendaId, Limit limite);

    /** Promedio (o {@code null} sin reseñas) y total de la tienda. */
    @Query("""
            select avg(resena.estrellas), count(resena) from ResenaEntity resena
            where resena.tiendaId = :tiendaId
            """)
    List<Object[]> resumenDe(@Param("tiendaId") UUID tiendaId);
}
