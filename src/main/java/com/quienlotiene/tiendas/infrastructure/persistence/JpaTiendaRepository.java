package com.quienlotiene.tiendas.infrastructure.persistence;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.tiendas.domain.model.DocumentoVerificacion;
import com.quienlotiene.tiendas.domain.model.EstadoTienda;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.model.TiendaNoEncontradaException;
import com.quienlotiene.tiendas.domain.model.TipoDocumento;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link TiendaRepository}. */
@Component
class JpaTiendaRepository implements TiendaRepository {

    private final EntityManager entityManager;
    private final TiendaJpaRepository tiendaJpaRepository;
    private final DocumentoVerificacionJpaRepository documentoJpaRepository;

    JpaTiendaRepository(
            EntityManager entityManager,
            TiendaJpaRepository tiendaJpaRepository,
            DocumentoVerificacionJpaRepository documentoJpaRepository) {
        this.entityManager = entityManager;
        this.tiendaJpaRepository = tiendaJpaRepository;
        this.documentoJpaRepository = documentoJpaRepository;
    }

    @Override
    public void guardarNueva(Tienda tienda, List<DocumentoVerificacion> documentos) {
        entityManager.persist(new TiendaEntity(
                tienda.getId(),
                tienda.getDuenoId(),
                tienda.getCategoriaId(),
                tienda.getNombre(),
                tienda.getTelefono(),
                tienda.getUbicacion().latitud(),
                tienda.getUbicacion().longitud(),
                tienda.getEstado().name(),
                tienda.estaAbierta(),
                tienda.getFechaRegistro()));
        for (var documento : documentos) {
            entityManager.persist(new DocumentoVerificacionEntity(
                    documento.id(),
                    documento.tiendaId(),
                    documento.tipo().name(),
                    documento.ruta(),
                    documento.tipoContenido(),
                    documento.tamanoEnBytes(),
                    documento.fechaSubida()));
        }
        // Si la base rechaza algo, que falle aquí y no al confirmar: así el caso de uso borra los archivos
        entityManager.flush();
    }

    @Override
    public Optional<Tienda> buscarPorId(UUID tiendaId) {
        return tiendaJpaRepository.findById(tiendaId).map(JpaTiendaRepository::aDominio);
    }

    @Override
    public List<Tienda> listarPorEstado(EstadoTienda estado) {
        return tiendaJpaRepository.findByEstadoOrderByCreadoEnAsc(estado.name()).stream()
                .map(JpaTiendaRepository::aDominio)
                .toList();
    }

    @Override
    public List<DocumentoVerificacion> listarDocumentos(UUID tiendaId) {
        return documentoJpaRepository.findByTiendaIdOrderByTipoAsc(tiendaId).stream()
                .map(fila -> new DocumentoVerificacion(
                        fila.getId(),
                        fila.getTiendaId(),
                        TipoDocumento.valueOf(fila.getTipo()),
                        fila.getRuta(),
                        fila.getTipoContenido(),
                        fila.getTamanoBytes(),
                        fila.getCreadoEn()))
                .toList();
    }

    @Override
    public void guardarRevision(Tienda tienda) {
        var fila = tiendaJpaRepository.findById(tienda.getId()).orElseThrow(TiendaNoEncontradaException::new);
        fila.registrarRevision(
                tienda.getEstado().name(),
                tienda.getMotivoRechazo().orElse(null),
                tienda.getFechaRevision().orElseThrow());
    }

    private static Tienda aDominio(TiendaEntity fila) {
        return Tienda.reconstituir(
                fila.getId(),
                fila.getDuenoId(),
                fila.getCategoriaId(),
                fila.getNombre(),
                fila.getTelefono(),
                new Ubicacion(fila.latitud(), fila.longitud()),
                EstadoTienda.valueOf(fila.getEstado()),
                fila.isAbierta(),
                fila.getCreadoEn(),
                fila.getMotivoRechazo(),
                fila.getFechaRevision());
    }
}
