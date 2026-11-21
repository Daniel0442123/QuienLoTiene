package com.quienlotiene.tiendas.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    /**
     * HU-037: suspende una tienda APROBADA (deja de recibir solicitudes y queda cerrada).
     *
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException TIE-006 si no existe, TIE-004 si no está APROBADA
     */
    TiendaSuspendida suspender(UUID tiendaId);

    /**
     * HU-037: vuelve a APROBADA una tienda SUSPENDIDA.
     *
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException TIE-006 si no existe, TIE-004 si no está SUSPENDIDA
     */
    TiendaSuspendida reactivar(UUID tiendaId);

    /** HU-037: nombre y estado de las tiendas pedidas, por id; las que no existen se omiten. */
    Map<UUID, TiendaResumen> resumenDe(Collection<UUID> tiendaIds);
}
