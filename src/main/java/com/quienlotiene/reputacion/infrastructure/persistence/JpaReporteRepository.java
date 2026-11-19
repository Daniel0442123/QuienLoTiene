package com.quienlotiene.reputacion.infrastructure.persistence;

import com.quienlotiene.reputacion.domain.model.EstadoReporte;
import com.quienlotiene.reputacion.domain.model.MotivoReporte;
import com.quienlotiene.reputacion.domain.model.Reporte;
import com.quienlotiene.reputacion.domain.port.ReporteRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link ReporteRepository}. */
@Component
class JpaReporteRepository implements ReporteRepository {

    private final ReporteJpaRepository reporteJpaRepository;

    JpaReporteRepository(ReporteJpaRepository reporteJpaRepository) {
        this.reporteJpaRepository = reporteJpaRepository;
    }

    @Override
    public void guardarNuevo(Reporte reporte) {
        reporteJpaRepository.save(new ReporteEntity(
                reporte.getId(),
                reporte.getOrdenId(),
                reporte.getTiendaId(),
                reporte.getAutorId(),
                reporte.autorEsCliente(),
                reporte.getMotivo().name(),
                reporte.getDescripcion(),
                reporte.getEstado().name(),
                reporte.getFecha()));
    }

    @Override
    public void guardarResolucion(Reporte reporte) {
        reporteJpaRepository
                .findById(reporte.getId())
                .orElseThrow()
                .resolver(reporte.getEstado().name(), reporte.getNotaResolucion(), reporte.getFechaResolucion());
    }

    @Override
    public Optional<Reporte> buscarPorId(UUID reporteId) {
        return reporteJpaRepository.findById(reporteId).map(JpaReporteRepository::aDominio);
    }

    @Override
    public List<Reporte> listarPorEstado(EstadoReporte estado) {
        return reporteJpaRepository.findByEstadoOrderByCreadoEnAsc(estado.name()).stream()
                .map(JpaReporteRepository::aDominio)
                .toList();
    }

    @Override
    public Map<UUID, Long> contarPorTienda(List<UUID> tiendaIds) {
        var conteo = new HashMap<UUID, Long>();
        if (tiendaIds.isEmpty()) {
            return conteo;
        }
        for (var fila : reporteJpaRepository.contarPorTienda(tiendaIds)) {
            conteo.put((UUID) fila[0], ((Number) fila[1]).longValue());
        }
        return conteo;
    }

    private static Reporte aDominio(ReporteEntity fila) {
        return Reporte.reconstituir(
                fila.getId(),
                fila.getOrdenId(),
                fila.getTiendaId(),
                fila.getAutorId(),
                fila.isAutorEsCliente(),
                MotivoReporte.valueOf(fila.getMotivo()),
                fila.getDescripcion(),
                fila.getCreadoEn(),
                EstadoReporte.valueOf(fila.getEstado()),
                fila.getNotaResolucion(),
                fila.getResueltoEn());
    }
}
