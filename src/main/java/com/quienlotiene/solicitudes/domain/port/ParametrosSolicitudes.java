package com.quienlotiene.solicitudes.domain.port;

import java.time.Duration;

/** Valores aprobados en la Fase 9, configurables para la demo sin recompilar (DECISIONES-BACKEND, sección 13). */
public interface ParametrosSolicitudes {

    /** Cuánto tiempo recibe respuestas una solicitud (30 minutos). */
    Duration vigencia();

    /** Máximo de solicitudes por cliente en una hora (10). */
    int maximoPorHora();

    /** Radio inicial de difusión (3 000 metros). */
    int radioInicialEnMetros();
}
