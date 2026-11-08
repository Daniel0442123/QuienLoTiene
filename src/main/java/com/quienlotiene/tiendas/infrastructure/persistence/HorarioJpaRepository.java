package com.quienlotiene.tiendas.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface HorarioJpaRepository extends JpaRepository<HorarioEntity, UUID> {

    List<HorarioEntity> findByTiendaIdOrderByDiaSemanaAsc(UUID tiendaId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from HorarioEntity horario where horario.tiendaId = :tiendaId")
    void borrarDeTienda(@Param("tiendaId") UUID tiendaId);
}
