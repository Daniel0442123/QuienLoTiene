package com.quienlotiene.reputacion.application.usecase;

import com.quienlotiene.reputacion.api.ReporteParaAdministrador;
import com.quienlotiene.reputacion.api.ReportesApi;
import com.quienlotiene.reputacion.domain.model.CalificacionInvalidaException;
import com.quienlotiene.reputacion.domain.model.EstadoReporte;
import com.quienlotiene.reputacion.domain.model.Reporte;
import com.quienlotiene.reputacion.domain.model.ResenaNoEncontradaException;
import com.quienlotiene.reputacion.domain.port.ReporteRepository;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa {@link ReportesApi}: el lado de reputacion de HU-037 (CU-27, diagrama 31). */
@Service
class AtenderReportes implements ReportesApi {

    private final ReporteRepository reporteRepository;
    private final Clock reloj;

    AtenderReportes(ReporteRepository reporteRepository, Clock reloj) {
        this.reporteRepository = reporteRepository;
        this.reloj = reloj;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteParaAdministrador> listarPorEstado(String estado) {
        EstadoReporte pedido;
        try {
            pedido = EstadoReporte.valueOf(estado.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException estadoDesconocido) {
            throw new CalificacionInvalidaException("el estado debe ser ABIERTO o RESUELTO");
        }
        var reportes = reporteRepository.listarPorEstado(pedido);
        var porTienda = reporteRepository.contarPorTienda(
                reportes.stream().map(Reporte::getTiendaId).distinct().toList());
        return reportes.stream().map(reporte -> aVista(reporte, porTienda)).toList();
    }

    @Override
    @Transactional
    public ReporteParaAdministrador resolver(UUID reporteId, String nota) {
        var reporte = reporteRepository
                .buscarPorId(reporteId)
                .orElseThrow(() -> new ResenaNoEncontradaException("El reporte"));
        reporte.resolver(nota, reloj.instant());
        reporteRepository.guardarResolucion(reporte);
        return aVista(reporte, reporteRepository.contarPorTienda(List.of(reporte.getTiendaId())));
    }

    private static ReporteParaAdministrador aVista(Reporte reporte, Map<UUID, Long> porTienda) {
        return new ReporteParaAdministrador(
                reporte.getId(),
                reporte.getOrdenId(),
                reporte.getTiendaId(),
                reporte.autorEsCliente() ? "CLIENTE" : "TIENDA",
                reporte.getMotivo().name(),
                reporte.getDescripcion(),
                reporte.getEstado().name(),
                reporte.getFecha(),
                reporte.getNotaResolucion(),
                reporte.getFechaResolucion(),
                porTienda.getOrDefault(reporte.getTiendaId(), 0L));
    }
}
