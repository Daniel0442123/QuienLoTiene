package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** El token de refresco no existe, venció o fue revocado: hay que iniciar sesión de nuevo. */
public class SesionVencidaException extends ReglaNegocioException {

    public SesionVencidaException() {
        super("IDE-010", "Sesión vencida", "Tu sesión terminó. Inicia sesión de nuevo.", TipoError.NO_AUTENTICADO);
    }
}
