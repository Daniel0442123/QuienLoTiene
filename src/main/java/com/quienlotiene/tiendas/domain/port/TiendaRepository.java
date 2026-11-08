package com.quienlotiene.tiendas.domain.port;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.tiendas.domain.model.DocumentoVerificacion;
import com.quienlotiene.tiendas.domain.model.EstadoTienda;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.model.TiendaCandidata;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Guarda las tiendas y sus documentos en el esquema {@code tiendas}. */
public interface TiendaRepository {

    void guardarNueva(Tienda tienda, List<DocumentoVerificacion> documentos);

    Optional<Tienda> buscarPorId(UUID tiendaId);

    /** La tienda del usuario con rol TIENDA (una por dueño). */
    Optional<Tienda> buscarPorDueno(UUID duenoId);

    /** De la más antigua a la más reciente. */
    List<Tienda> listarPorEstado(EstadoTienda estado);

    List<DocumentoVerificacion> listarDocumentos(UUID tiendaId);

    /** Guarda el estado, el motivo y la fecha de revisión de una tienda existente. */
    void guardarRevision(Tienda tienda);

    /** Guarda el nombre, el teléfono y el horario (HU-009). */
    void guardarPerfil(Tienda tienda);

    /** Guarda el interruptor de apertura (HU-013). */
    void guardarApertura(Tienda tienda);

    /**
     * Tiendas APROBADAS y abiertas de la categoría cuyo local está a {@code radioEnMetros} o menos del punto, de la más
     * cercana a la más lejana (PostGIS {@code ST_DWithin}). El horario lo revisa el dominio.
     */
    List<TiendaCandidata> buscarCandidatasCerca(UUID categoriaId, Ubicacion punto, int radioEnMetros);
}
