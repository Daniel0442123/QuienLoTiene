package com.quienlotiene.solicitudes.domain.port;

import com.quienlotiene.solicitudes.domain.model.Respuesta;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Respuestas de las tiendas ({@code solicitudes.respuesta}). */
public interface RespuestaRepository {

    /**
     * @throws com.quienlotiene.solicitudes.domain.model.RespuestaDuplicadaException si otra petición de la misma
     *     tienda ganó la carrera (restricción única)
     */
    void guardarNueva(Respuesta respuesta);

    /** Guarda el id de la orden de la respuesta elegida (HU-024). */
    void guardarEleccion(Respuesta respuesta);

    boolean existe(UUID solicitudId, UUID tiendaId);

    Optional<Respuesta> buscarPorId(UUID respuestaId);

    /** De la primera a la última en llegar. */
    List<Respuesta> listarDe(UUID solicitudId);

    /** Respuestas SI y PARECIDO de la tienda desde {@code desde}, de la más reciente a la más antigua. */
    List<Respuesta> listarConProductoDeTiendaDesde(UUID tiendaId, Instant desde);

    /**
     * SC-011: último precio por unidad que la tienda dio con SI para el mismo producto (sin importar mayúsculas ni
     * espacios al inicio o al final).
     */
    Optional<Long> buscarUltimoPrecio(UUID tiendaId, String producto);

    /** HU-008: cuántas respuestas tiene cada solicitud (las que no tienen ninguna no aparecen). */
    Map<UUID, Integer> contarPorSolicitud(Collection<UUID> solicitudIds);

    /** HU-008: la orden creada en cada solicitud RESUELTA (las demás no aparecen). */
    Map<UUID, UUID> ordenesPorSolicitud(Collection<UUID> solicitudIds);
}
