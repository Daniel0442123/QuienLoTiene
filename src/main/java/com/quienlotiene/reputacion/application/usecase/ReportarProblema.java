package com.quienlotiene.reputacion.application.usecase;

import com.quienlotiene.ordenes.api.OrdenesApi;
import com.quienlotiene.reputacion.application.dto.ReportarComando;
import com.quienlotiene.reputacion.application.dto.ReporteVista;
import com.quienlotiene.reputacion.domain.model.CalificacionInvalidaException;
import com.quienlotiene.reputacion.domain.model.MotivoReporte;
import com.quienlotiene.reputacion.domain.model.Reporte;
import com.quienlotiene.reputacion.domain.port.ReporteRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-035 Reportar problema (CU-13, diagrama 19): solo quien participa en la orden (CP-REP-035-02). El reporte queda
 * ABIERTO para el administrador (CP-REP-035-01).
 */
@Slf4j
@Service
public class ReportarProblema {

    private final OrdenesApi ordenesApi;
    private final ReporteRepository reporteRepository;
    private final Clock reloj;

    ReportarProblema(OrdenesApi ordenesApi, ReporteRepository reporteRepository, Clock reloj) {
        this.ordenesApi = ordenesApi;
        this.reporteRepository = reporteRepository;
        this.reloj = reloj;
    }

    /** @throws com.quienlotiene.shared.domain.ReglaNegocioException ORD-001 si la orden no es del usuario */
    @Transactional
    public ReporteVista ejecutar(ReportarComando comando) {
        var orden = ordenesApi.obtenerResumen(comando.ordenId(), comando.usuarioId());
        var reporte = Reporte.crear(
                UUID.randomUUID(),
                orden.ordenId(),
                orden.tiendaId(),
                comando.usuarioId(),
                orden.esDelCliente(comando.usuarioId()),
                motivoDe(comando.motivo()),
                comando.descripcion(),
                reloj.instant());
        reporteRepository.guardarNuevo(reporte);
        log.info("Reporte {} sobre la orden {} ({})", reporte.getId(), orden.ordenId(), reporte.getMotivo());
        return new ReporteVista(
                reporte.getId(),
                reporte.getOrdenId(),
                reporte.getMotivo().name(),
                reporte.getDescripcion(),
                reporte.getEstado().name(),
                reporte.getFecha());
    }

    private static MotivoReporte motivoDe(String motivo) {
        if (motivo == null) {
            return null;
        }
        try {
            return MotivoReporte.valueOf(motivo);
        } catch (IllegalArgumentException motivoDesconocido) {
            throw new CalificacionInvalidaException(
                    "el motivo debe ser DEMORA_ENTREGA, PRODUCTO_DIFERENTE, COBRO_DIFERENTE u OTRO");
        }
    }
}
