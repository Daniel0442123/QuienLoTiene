package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ConTiempoDeEspera;
import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;
import java.time.Duration;

/** NF-SEG-009: la cuenta ya usó sus 5 intentos de inicio de sesión del último minuto (HTTP 429 con Retry-After). */
public class DemasiadosIntentosInicioSesionException extends ReglaNegocioException implements ConTiempoDeEspera {

    private final Duration tiempoDeEspera;

    public DemasiadosIntentosInicioSesionException(Duration tiempoDeEspera) {
        super(
                "IDE-015",
                "Demasiados intentos",
                "Hiciste " + IntentosInicioSesion.MAXIMO_POR_MINUTO
                        + " intentos de inicio de sesión en un minuto. Espera un momento y vuelve a intentar.",
                TipoError.DEMASIADOS_INTENTOS);
        this.tiempoDeEspera = tiempoDeEspera.isNegative() ? Duration.ZERO : tiempoDeEspera;
    }

    @Override
    public Duration tiempoDeEspera() {
        return tiempoDeEspera;
    }
}
