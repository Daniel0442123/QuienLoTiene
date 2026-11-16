package com.quienlotiene.solicitudes.infrastructure.scheduling;

import com.quienlotiene.solicitudes.application.usecase.VencerSolicitudes;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** CU-29: cada minuto vence las solicitudes que pasaron sus 30 minutos sin elección (D-19, diagrama 33 paso 1). */
@Component
class VencimientoSolicitudesTarea {

    private final VencerSolicitudes vencerSolicitudes;

    VencimientoSolicitudesTarea(VencerSolicitudes vencerSolicitudes) {
        this.vencerSolicitudes = vencerSolicitudes;
    }

    @Scheduled(fixedDelayString = "${quienlotiene.solicitudes.intervalo-vencimiento:PT1M}")
    void vencer() {
        vencerSolicitudes.ejecutar();
    }
}
