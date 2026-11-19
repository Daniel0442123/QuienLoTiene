package com.quienlotiene.ordenes.application.usecase;

import com.quienlotiene.ordenes.application.dto.OrdenVista;
import com.quienlotiene.ordenes.domain.model.OrdenNoEncontradaException;
import com.quienlotiene.ordenes.domain.port.OrdenRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-029 Cancelar orden (CU-08, diagrama 15): el cliente mientras la orden no salió de la tienda; la tienda solo en
 * CONFIRMADO. La otra parte recibe el aviso con el motivo y la tienda deja de ver los datos del cliente (SC-011 punto 9).
 */
@Slf4j
@Service
public class CancelarOrden {

    private final OrdenRepository ordenRepository;
    private final ApplicationEventPublisher publicadorEventos;
    private final Clock reloj;

    CancelarOrden(OrdenRepository ordenRepository, ApplicationEventPublisher publicadorEventos, Clock reloj) {
        this.ordenRepository = ordenRepository;
        this.publicadorEventos = publicadorEventos;
        this.reloj = reloj;
    }

    /**
     * @throws OrdenNoEncontradaException si no existe o el usuario no es parte de ella (R-03)
     * @throws com.quienlotiene.ordenes.domain.model.OrdenYaDespachadaException ORD-005 (CP-ORD-029-02)
     * @throws com.quienlotiene.ordenes.domain.model.TransicionOrdenInvalidaException ORD-002
     */
    @Transactional
    public OrdenVista ejecutar(UUID usuarioId, UUID ordenId, String motivo) {
        var orden = ordenRepository
                .buscarPorId(ordenId)
                .filter(encontrada -> encontrada.esParte(usuarioId))
                .orElseThrow(OrdenNoEncontradaException::new);
        if (orden.esDelCliente(usuarioId)) {
            orden.cancelarPorCliente(motivo, reloj.instant());
        } else {
            orden.cancelarPorTienda(motivo, reloj.instant());
        }
        ordenRepository.guardarCambioEstado(orden);
        CambiarEstadoOrden.publicar(publicadorEventos, orden);
        log.info("Orden {} cancelada por {}", orden.getId(), orden.getCanceladaPor());
        return OrdenVistaMapper.aVista(orden, usuarioId);
    }
}
