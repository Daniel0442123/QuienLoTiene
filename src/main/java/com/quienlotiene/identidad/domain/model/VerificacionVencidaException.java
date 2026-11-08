package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** El token del segundo factor no existe, venció, ya se usó o agotó sus 5 intentos. */
public class VerificacionVencidaException extends ReglaNegocioException {

    public VerificacionVencidaException() {
        super(
                "IDE-013",
                "Verificación vencida",
                "La verificación venció o superó los intentos permitidos. Inicia sesión de nuevo.",
                TipoError.NO_AUTENTICADO);
    }
}
