package com.quienlotiene.ordenes.application.usecase;

import com.quienlotiene.ordenes.api.EstadoOrdenCambiadoEvent;
import com.quienlotiene.ordenes.application.dto.OrdenVista;
import com.quienlotiene.ordenes.domain.model.EstadoOrden;
import com.quienlotiene.ordenes.domain.model.Orden;
import com.quienlotiene.ordenes.domain.model.OrdenNoEncontradaException;
import com.quienlotiene.ordenes.domain.model.TransicionOrdenInvalidaException;
import com.quienlotiene.ordenes.domain.port.OrdenRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-027 Actualizar estado de la orden (diagrama 04 pasos 10 a 15): solo la tienda de la orden y solo el paso
 * siguiente válido. El cliente se entera con {@link EstadoOrdenCambiadoEvent}.
 */
@Slf4j
@Service
public class CambiarEstadoOrden {

    private final OrdenRepository ordenRepository;
    private final ApplicationEventPublisher publicadorEventos;
    private final Clock reloj;

    CambiarEstadoOrden(OrdenRepository ordenRepository, ApplicationEventPublisher publicadorEventos, Clock reloj) {
        this.ordenRepository = ordenRepository;
        this.publicadorEventos = publicadorEventos;
        this.reloj = reloj;
    }

    @Transactional
    public OrdenVista ejecutar(UUID duenoTiendaId, UUID ordenId, String estadoNuevo) {
        var orden = ordenRepository
                .buscarPorId(ordenId)
                .filter(encontrada -> encontrada.esDeLaTienda(duenoTiendaId))
                .orElseThrow(OrdenNoEncontradaException::new);
        var siguiente = estadoDe(estadoNuevo, orden);
        orden.avanzarPorTienda(siguiente, reloj.instant());
        ordenRepository.guardarCambioEstado(orden);
        publicar(publicadorEventos, orden);
        log.info("Orden {} pasó a {}", orden.getId(), orden.getEstado());
        return OrdenVistaMapper.aVista(orden, duenoTiendaId);
    }

    static void publicar(ApplicationEventPublisher publicadorEventos, Orden orden) {
        publicadorEventos.publishEvent(new EstadoOrdenCambiadoEvent(
                orden.getId(),
                orden.getClienteId(),
                orden.getDuenoTiendaId(),
                orden.getEstado().name()));
    }

    private static EstadoOrden estadoDe(String estado, Orden orden) {
        if (estado == null) {
            throw new TransicionOrdenInvalidaException(orden.getEstado(), EstadoOrden.CONFIRMADO);
        }
        try {
            return EstadoOrden.valueOf(estado);
        } catch (IllegalArgumentException estadoDesconocido) {
            throw new TransicionOrdenInvalidaException(orden.getEstado(), EstadoOrden.CONFIRMADO);
        }
    }
}
