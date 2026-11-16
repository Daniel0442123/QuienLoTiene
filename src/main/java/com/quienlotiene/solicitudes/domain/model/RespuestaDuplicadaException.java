package com.quienlotiene.solicitudes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-SOL-019-04: la tienda ya respondió esta solicitud. */
public class RespuestaDuplicadaException extends ReglaNegocioException {

    public RespuestaDuplicadaException() {
        super("SOL-005", "Respuesta duplicada", "Ya respondiste esta solicitud.", TipoError.CONFLICTO);
    }
}
