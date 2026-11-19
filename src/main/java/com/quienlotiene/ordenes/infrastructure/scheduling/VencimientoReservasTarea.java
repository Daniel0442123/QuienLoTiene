package com.quienlotiene.ordenes.infrastructure.scheduling;

import com.quienlotiene.ordenes.application.usecase.VencerReservas;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** CU-30: cada minuto vence las recogidas que pasaron su reserva (diagrama 34 paso 1). */
@Component
class VencimientoReservasTarea {

    private final VencerReservas vencerReservas;

    VencimientoReservasTarea(VencerReservas vencerReservas) {
        this.vencerReservas = vencerReservas;
    }

    @Scheduled(fixedDelayString = "${quienlotiene.ordenes.intervalo-vencimiento:PT1M}")
    void vencer() {
        vencerReservas.ejecutar();
    }
}
