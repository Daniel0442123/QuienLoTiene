package com.quienlotiene.reputacion.domain.port;

import com.quienlotiene.reputacion.domain.model.EstadoReporte;
import com.quienlotiene.reputacion.domain.model.Reporte;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Reportes del esquema {@code reputacion}. */
public interface ReporteRepository {

    void guardarNuevo(Reporte reporte);

    /** Guarda el estado, la nota y la fecha de resolución. */
    void guardarResolucion(Reporte reporte);

    Optional<Reporte> buscarPorId(UUID reporteId);

    /** Del más antiguo al más reciente. */
    List<Reporte> listarPorEstado(EstadoReporte estado);

    /** Cuántos reportes (abiertos y resueltos) tiene cada tienda pedida. */
    Map<UUID, Long> contarPorTienda(List<UUID> tiendaIds);
}
