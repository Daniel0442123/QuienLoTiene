package com.quienlotiene.solicitudes.infrastructure.persistence;

import com.quienlotiene.solicitudes.domain.model.Respuesta;
import com.quienlotiene.solicitudes.domain.model.RespuestaDuplicadaException;
import com.quienlotiene.solicitudes.domain.model.TipoRespuesta;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link RespuestaRepository}. */
@Component
class JpaRespuestaRepository implements RespuestaRepository {

    private final RespuestaJpaRepository respuestaJpaRepository;
    private final EntityManager entityManager;
    private final Clock reloj;

    JpaRespuestaRepository(RespuestaJpaRepository respuestaJpaRepository, EntityManager entityManager, Clock reloj) {
        this.respuestaJpaRepository = respuestaJpaRepository;
        this.entityManager = entityManager;
        this.reloj = reloj;
    }

    @Override
    public void guardarNueva(Respuesta respuesta) {
        entityManager.persist(new RespuestaEntity(
                respuesta.getId(),
                respuesta.getSolicitudId(),
                respuesta.getTiendaId(),
                respuesta.getDuenoId(),
                respuesta.getTipo().name(),
                respuesta.getPrecioEnPesos(),
                respuesta.getCantidadDisponible(),
                respuesta.getDescripcionAlternativa(),
                respuesta.getTiempoEstimadoEnMinutos(),
                respuesta.getCostoDomicilioEnPesos(),
                respuesta.getFechaRespuesta()));
        try {
            // Dos toques seguidos de la misma tienda: la restricción única decide y se responde SOL-005
            entityManager.flush();
        } catch (PersistenceException carreraPerdida) {
            throw new RespuestaDuplicadaException();
        }
    }

    @Override
    public void guardarEleccion(Respuesta respuesta) {
        var fila = respuestaJpaRepository.findById(respuesta.getId()).orElseThrow();
        fila.elegir(respuesta.getOrdenId(), reloj.instant());
    }

    @Override
    public boolean existe(UUID solicitudId, UUID tiendaId) {
        return respuestaJpaRepository.existsBySolicitudIdAndTiendaId(solicitudId, tiendaId);
    }

    @Override
    public Optional<Respuesta> buscarPorId(UUID respuestaId) {
        return respuestaJpaRepository.findById(respuestaId).map(JpaRespuestaRepository::aDominio);
    }

    @Override
    public List<Respuesta> listarDe(UUID solicitudId) {
        return respuestaJpaRepository.findBySolicitudIdOrderByCreadoEnAsc(solicitudId).stream()
                .map(JpaRespuestaRepository::aDominio)
                .toList();
    }

    @Override
    public List<Respuesta> listarConProductoDeTiendaDesde(UUID tiendaId, Instant desde) {
        return respuestaJpaRepository.listarConProductoDeTiendaDesde(tiendaId, desde).stream()
                .map(JpaRespuestaRepository::aDominio)
                .toList();
    }

    @Override
    public Optional<Long> buscarUltimoPrecio(UUID tiendaId, String producto) {
        return respuestaJpaRepository.listarPreciosDelProducto(tiendaId, producto, Limit.of(1)).stream()
                .findFirst();
    }

    private static Respuesta aDominio(RespuestaEntity fila) {
        return Respuesta.reconstituir(
                fila.getId(),
                fila.getSolicitudId(),
                fila.getTiendaId(),
                fila.getDuenoId(),
                TipoRespuesta.valueOf(fila.getTipo()),
                fila.getPrecioPesos(),
                fila.getCantidadDisponible(),
                fila.getDescripcionAlternativa(),
                fila.getTiempoEstimadoMinutos(),
                fila.getCostoDomicilioPesos(),
                fila.getCreadoEn(),
                fila.getOrdenId());
    }
}
