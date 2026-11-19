package com.quienlotiene.reputacion.api;

import java.util.List;
import java.util.UUID;

/** Fachada para que administracion atienda los reportes (HU-037, CU-27). La auditoría la registra administracion. */
public interface ReportesApi {

    /** Reportes en ese estado ({@code ABIERTO} o {@code RESUELTO}), del más antiguo al más reciente. */
    List<ReporteParaAdministrador> listarPorEstado(String estado);

    /**
     * CP-ADM-037-02: deja el reporte RESUELTO con la nota.
     *
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException REP-003 si no existe, REP-005 si ya estaba
     *     resuelto, REP-004 si la nota es muy corta o muy larga
     */
    ReporteParaAdministrador resolver(UUID reporteId, String nota);
}
