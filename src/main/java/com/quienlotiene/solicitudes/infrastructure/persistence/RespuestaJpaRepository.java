package com.quienlotiene.solicitudes.infrastructure.persistence;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface RespuestaJpaRepository extends JpaRepository<RespuestaEntity, UUID> {

    @Query("""
            select respuesta.solicitudId, count(respuesta) from RespuestaEntity respuesta
            where respuesta.solicitudId in :solicitudIds
            group by respuesta.solicitudId
            """)
    List<Object[]> contarPorSolicitud(@Param("solicitudIds") Collection<UUID> solicitudIds);

    @Query("""
            select respuesta.solicitudId, respuesta.ordenId from RespuestaEntity respuesta
            where respuesta.solicitudId in :solicitudIds and respuesta.ordenId is not null
            """)
    List<Object[]> ordenesPorSolicitud(@Param("solicitudIds") Collection<UUID> solicitudIds);

    boolean existsBySolicitudIdAndTiendaId(UUID solicitudId, UUID tiendaId);

    List<RespuestaEntity> findBySolicitudIdOrderByCreadoEnAsc(UUID solicitudId);

    @Query("""
            select respuesta from RespuestaEntity respuesta
            where respuesta.tiendaId = :tiendaId and respuesta.tipo <> 'NO' and respuesta.creadoEn >= :desde
            order by respuesta.creadoEn desc
            """)
    List<RespuestaEntity> listarConProductoDeTiendaDesde(
            @Param("tiendaId") UUID tiendaId, @Param("desde") Instant desde);

    @Query("""
            select respuesta.precioPesos from RespuestaEntity respuesta, SolicitudEntity solicitud
            where solicitud.id = respuesta.solicitudId and respuesta.tiendaId = :tiendaId and respuesta.tipo = 'SI'
              and lower(trim(solicitud.producto)) = lower(trim(:producto))
            order by respuesta.creadoEn desc
            """)
    List<Long> listarPreciosDelProducto(
            @Param("tiendaId") UUID tiendaId, @Param("producto") String producto, Limit limite);
}
