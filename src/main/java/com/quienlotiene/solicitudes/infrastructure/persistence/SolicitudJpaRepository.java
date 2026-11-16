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

    /** HU-018: cada fila es [SolicitudEntity, distancia en metros del envío]. */
    @Query("""
            select solicitud, envio.distanciaMetros
            from SolicitudEntity solicitud, EnvioSolicitudEntity envio
            where envio.solicitudId = solicitud.id and envio.tiendaId = :tiendaId
              and solicitud.estado = 'ABIERTA' and solicitud.venceEn > :ahora
              and not exists (
                  select 1 from RespuestaEntity respuesta
                  where respuesta.solicitudId = solicitud.id and respuesta.tiendaId = :tiendaId)
            order by solicitud.urgente desc, solicitud.creadoEn desc
            """)
    List<Object[]> listarDisponiblesParaTienda(@Param("tiendaId") UUID tiendaId, @Param("ahora") Instant ahora);

    /** CU-29: SQL nativo por {@code FOR UPDATE SKIP LOCKED} (D-19, D-23). */
    @Query(value = """
                    select * from solicitudes.solicitud
                    where estado = 'ABIERTA' and vence_en <= :ahora
                    order by vence_en
                    limit :limite
                    for update skip locked
                    """, nativeQuery = true)
    List<SolicitudEntity> tomarVencidas(@Param("ahora") Instant ahora, @Param("limite") int limite);
}
