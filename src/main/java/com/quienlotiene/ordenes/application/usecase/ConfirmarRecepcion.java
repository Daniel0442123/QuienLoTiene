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
 * HU-028 Confirmar recepción (diagrama 04 pasos 16 a 19): solo el cliente de la orden y solo si está ENTREGADO. La
 * orden queda RESUELTA y la tienda deja de ver los datos del cliente (CP-ORD-028-01).
 */
@Slf4j
@Service
public class ConfirmarRecepcion {

    private final OrdenRepository ordenRepository;
    private final ApplicationEventPublisher publicadorEventos;
    private final Clock reloj;

    ConfirmarRecepcion(OrdenRepository ordenRepository, ApplicationEventPublisher publicadorEventos, Clock reloj) {
        this.ordenRepository = ordenRepository;
        this.publicadorEventos = publicadorEventos;
        this.reloj = reloj;
    }

    /**
     * @throws OrdenNoEncontradaException si no existe o es de otro cliente (CP-ORD-028-02)
     */
    @Transactional
    public OrdenVista ejecutar(UUID clienteId, UUID ordenId) {
        var orden = ordenRepository
                .buscarPorId(ordenId)
                .filter(encontrada -> encontrada.esDelCliente(clienteId))
                .orElseThrow(OrdenNoEncontradaException::new);
        orden.confirmarRecepcion(reloj.instant());
        ordenRepository.guardarCambioEstado(orden);
        CambiarEstadoOrden.publicar(publicadorEventos, orden);
        log.info("Orden {} resuelta por su cliente", orden.getId());
        return OrdenVistaMapper.aVista(orden, clienteId);
    }
}
