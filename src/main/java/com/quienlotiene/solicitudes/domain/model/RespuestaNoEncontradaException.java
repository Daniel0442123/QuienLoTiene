package com.quienlotiene.solicitudes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** La respuesta elegida no existe o no es de una solicitud del cliente (R-03). */
public class RespuestaNoEncontradaException extends ReglaNegocioException {

    public RespuestaNoEncontradaException() {
        super("SOL-007", "Respuesta no encontrada", "La respuesta no existe.", TipoError.NO_ENCONTRADO);
    }
}
