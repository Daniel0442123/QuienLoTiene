package com.quienlotiene.solicitudes.infrastructure.persistence;

import com.quienlotiene.solicitudes.domain.model.ActividadDeTienda;
import com.quienlotiene.solicitudes.domain.port.ActividadDeTiendaRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Adaptador de {@link ActividadDeTiendaRepository}: una sola consulta de conteos con los índices
 * {@code ix_envio_solicitud_tienda} e {@code ix_respuesta_tienda_creado} (D-23, solo SQL con parámetros).
 */
@Component
class JdbcActividadDeTiendaRepository implements ActividadDeTiendaRepository {

    private final JdbcClient jdbcClient;

    JdbcActividadDeTiendaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public ActividadDeTienda contarDesde(UUID tiendaId, Instant desde) {
        return jdbcClient
                .sql("""
                        select
                            (select count(*) from solicitudes.envio_solicitud envio
                             where envio.tienda_id = :tiendaId and envio.creado_en >= :desde) as recibidas,
                            count(respuesta.id) as respondidas,
                            count(respuesta.id) filter (where respuesta.tipo in ('SI', 'PARECIDO')) as con_producto,
                            count(respuesta.id) filter (where respuesta.orden_id is not null) as ganadas
                        from solicitudes.respuesta respuesta
                        where respuesta.tienda_id = :tiendaId and respuesta.creado_en >= :desde
                        """)
                .param("tiendaId", tiendaId)
                .param("desde", Timestamp.from(desde))
                .query((fila, numero) -> new ActividadDeTienda(
                        fila.getInt("recibidas"),
                        fila.getInt("respondidas"),
                        fila.getInt("con_producto"),
                        fila.getInt("ganadas")))
                .single();
    }
}
