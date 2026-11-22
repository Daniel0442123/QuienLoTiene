package com.quienlotiene.solicitudes.infrastructure.scheduling;

import com.quienlotiene.solicitudes.application.usecase.AmpliarRadioSolicitudes;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** CU-31: cada minuto amplía el radio de las solicitudes que siguen sin respuestas (diagrama 35 paso 1). */
@Component
class AmpliacionRadioTarea {

    private final AmpliarRadioSolicitudes ampliarRadioSolicitudes;

    AmpliacionRadioTarea(AmpliarRadioSolicitudes ampliarRadioSolicitudes) {
        this.ampliarRadioSolicitudes = ampliarRadioSolicitudes;
    }

    @Scheduled(fixedDelayString = "${quienlotiene.solicitudes.intervalo-ampliacion:PT1M}")
    void ampliar() {
        ampliarRadioSolicitudes.ejecutar();
    }
}
