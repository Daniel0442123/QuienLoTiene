package com.quienlotiene.solicitudes.domain.model;

import com.quienlotiene.shared.domain.ConTiempoDeEspera;
import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;
import java.time.Duration;

/** CP-SOL-016-03: el cliente ya publicó el máximo de solicitudes de la última hora. */
public class LimiteSolicitudesSuperadoException extends ReglaNegocioException implements ConTiempoDeEspera {

    private final Duration tiempoDeEspera;

    public LimiteSolicitudesSuperadoException(int maximoPorHora, Duration tiempoDeEspera) {
        super(
                "SOL-001",
                "Límite de solicitudes superado",
                "Puedes publicar hasta " + maximoPorHora + " solicitudes por hora. Intenta de nuevo en "
                        + minutosHaciaArriba(tiempoDeEspera) + ".",
                TipoError.DEMASIADOS_INTENTOS);
        this.tiempoDeEspera = tiempoDeEspera;
    }

    @Override
    public Duration tiempoDeEspera() {
        return tiempoDeEspera;
    }

    private static String minutosHaciaArriba(Duration espera) {
        var minutos = Math.max(1, (espera.toSeconds() + 59) / 60);
        return minutos == 1 ? "1 minuto" : minutos + " minutos";
    }
}
