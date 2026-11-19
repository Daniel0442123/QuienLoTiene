package com.quienlotiene.ordenes.infrastructure.persistence;

import java.time.Instant;
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

    /** CU-30: SQL nativo por {@code FOR UPDATE SKIP LOCKED} (D-19, D-23), igual que el vencimiento de solicitudes. */
    @Query(value = """
                    select * from ordenes.orden
                    where estado = 'LISTO_PARA_RECOGER' and reserva_vence <= :ahora
                    order by reserva_vence
                    limit :limite
                    for update skip locked
                    """, nativeQuery = true)
    List<OrdenEntity> tomarReservasVencidas(@Param("ahora") Instant ahora, @Param("limite") int limite);
}
