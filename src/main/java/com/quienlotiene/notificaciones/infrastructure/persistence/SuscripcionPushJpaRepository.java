package com.quienlotiene.notificaciones.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SuscripcionPushJpaRepository extends JpaRepository<SuscripcionPushEntity, UUID> {

    List<SuscripcionPushEntity> findByUsuarioId(UUID usuarioId);

    @Modifying(flushAutomatically = true)
    @Query("delete from SuscripcionPushEntity suscripcion where suscripcion.endpoint = :endpoint")
    void borrarPorEndpoint(@Param("endpoint") String endpoint);

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from SuscripcionPushEntity suscripcion
            where suscripcion.usuarioId = :usuarioId and suscripcion.endpoint = :endpoint
            """)
    void borrarDelUsuario(@Param("usuarioId") UUID usuarioId, @Param("endpoint") String endpoint);
}
