package com.quienlotiene.administracion.application.usecase;

import com.quienlotiene.administracion.domain.model.AccionAuditada;
import com.quienlotiene.administracion.domain.model.RegistroAuditoria;
import com.quienlotiene.administracion.domain.port.AuditoriaRepository;
import com.quienlotiene.identidad.api.CuentasApi;
import com.quienlotiene.reputacion.api.ReporteParaAdministrador;
import com.quienlotiene.reputacion.api.ReportesApi;
import com.quienlotiene.tiendas.api.RevisionTiendasApi;
import com.quienlotiene.tiendas.api.TiendaParaRevision;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-037 Gestionar reportes y suspender cuentas (CU-27, diagrama 31). Cada acción queda en la auditoría en la misma
 * transacción (RNF-013). Suspender una tienda también suspende la cuenta de su dueño y cierra sus sesiones
 * (CP-ADM-037-01).
 */
@Service
public class GestionarReportesYSuspensiones {

    private final ReportesApi reportesApi;
    private final RevisionTiendasApi revisionTiendasApi;
    private final CuentasApi cuentasApi;
    private final AuditoriaRepository auditoriaRepository;
    private final Clock reloj;

    GestionarReportesYSuspensiones(
            ReportesApi reportesApi,
            RevisionTiendasApi revisionTiendasApi,
            CuentasApi cuentasApi,
            AuditoriaRepository auditoriaRepository,
            Clock reloj) {
        this.reportesApi = reportesApi;
        this.revisionTiendasApi = revisionTiendasApi;
        this.cuentasApi = cuentasApi;
        this.auditoriaRepository = auditoriaRepository;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<ReporteParaAdministrador> listarReportes(String estado) {
        return reportesApi.listarPorEstado(estado);
    }

    /** CP-ADM-037-02: el reporte queda RESUELTO y la acción queda auditada. */
    @Transactional
    public ReporteParaAdministrador resolverReporte(UUID administradorId, UUID reporteId, String nota) {
        var reporte = reportesApi.resolver(reporteId, nota);
        auditar(administradorId, AccionAuditada.REPORTE_RESUELTO, reporteId, reporte.notaResolucion());
        return reporte;
    }

    /** CP-ADM-037-01: la tienda no recibe solicitudes y su dueño no puede iniciar sesión. */
    @Transactional
    public TiendaParaRevision suspenderTienda(UUID administradorId, UUID tiendaId, String motivo) {
        var suspendida = revisionTiendasApi.suspender(tiendaId);
        cuentasApi.suspender(suspendida.duenoId());
        auditar(administradorId, AccionAuditada.TIENDA_SUSPENDIDA, tiendaId, motivo);
        return suspendida.tienda();
    }

    @Transactional
    public TiendaParaRevision reactivarTienda(UUID administradorId, UUID tiendaId) {
        var reactivada = revisionTiendasApi.reactivar(tiendaId);
        cuentasApi.reactivar(reactivada.duenoId());
        auditar(administradorId, AccionAuditada.TIENDA_REACTIVADA, tiendaId, null);
        return reactivada.tienda();
    }

    private void auditar(UUID administradorId, AccionAuditada accion, UUID recursoId, String detalle) {
        auditoriaRepository.registrar(
                new RegistroAuditoria(UUID.randomUUID(), administradorId, accion, recursoId, detalle, reloj.instant()));
    }
}
