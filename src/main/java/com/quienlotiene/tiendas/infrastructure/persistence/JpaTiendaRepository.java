package com.quienlotiene.tiendas.infrastructure.persistence;

import com.quienlotiene.tiendas.domain.model.DocumentoVerificacion;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link TiendaRepository}. */
@Component
class JpaTiendaRepository implements TiendaRepository {

    private final EntityManager entityManager;

    JpaTiendaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
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
}
