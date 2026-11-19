package com.quienlotiene.ordenes.application.usecase;

import com.quienlotiene.ordenes.domain.port.OrdenRepository;
import java.time.Clock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-025 Vencer reservas no recogidas (CU-30, diagrama 34): las recogidas LISTO_PARA_RECOGER que pasaron su reserva
 * quedan VENCIDAS y se avisa al cliente y a la tienda (CP-ORD-025-01). La llama la tarea programada cada minuto.
 */
@Slf4j
@Service
public class VencerReservas {

    /** Cuántas órdenes procesa cada ejecución; las demás quedan para el minuto siguiente. */
    static final int LOTE = 100;

    private final OrdenRepository ordenRepository;
    private final ApplicationEventPublisher publicadorEventos;
    private final Clock reloj;

    VencerReservas(OrdenRepository ordenRepository, ApplicationEventPublisher publicadorEventos, Clock reloj) {
        this.ordenRepository = ordenRepository;
        this.publicadorEventos = publicadorEventos;
        this.reloj = reloj;
    }

    /** @return cuántas reservas vencieron en esta ejecución */
    @Transactional
    public int ejecutar() {
        var ahora = reloj.instant();
        var vencidas = 0;
        for (var orden : ordenRepository.tomarReservasVencidas(ahora, LOTE)) {
            if (!orden.vencerReserva(ahora)) {
                continue;
            }
            ordenRepository.guardarCambioEstado(orden);
            CambiarEstadoOrden.publicar(publicadorEventos, orden);
            log.info("Reserva de la orden {} vencida sin recoger", orden.getId());
            vencidas++;
        }
        return vencidas;
    }
}
