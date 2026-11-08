package com.quienlotiene.tiendas.api;

import java.util.List;
import java.util.UUID;

/**
 * Fachada para que el módulo administracion revise las tiendas (HU-036, CU-26). La transición de estado y sus reglas
 * viven en el dominio de tiendas; administracion solo decide y registra la auditoría.
 */
public interface RevisionTiendasApi {

    /** Tiendas en el estado pedido, de la más antigua a la más reciente (la bandeja usa PENDIENTE). */
    List<TiendaParaRevision> listarPorEstado(String estado);

    /**
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException TIE-006 si la tienda o el documento no existe
     */
    DocumentoDescargable obtenerDocumento(UUID tiendaId, UUID documentoId);

    /** Aprueba una tienda PENDIENTE y publica {@link TiendaRevisadaEvent}. */
    TiendaParaRevision aprobar(UUID tiendaId);

    /** Rechaza una tienda PENDIENTE con su motivo y publica {@link TiendaRevisadaEvent}. */
    TiendaParaRevision rechazar(UUID tiendaId, String motivo);
}
