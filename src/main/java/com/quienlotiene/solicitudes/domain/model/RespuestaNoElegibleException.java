package com.quienlotiene.solicitudes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** El cliente intenta elegir una respuesta NO. */
public class RespuestaNoElegibleException extends ReglaNegocioException {

    public RespuestaNoElegibleException() {
        super(
                "SOL-008",
                "Respuesta no elegible",
                "No puedes elegir una respuesta «No lo tengo».",
                TipoError.REGLA_NEGOCIO);
    }
}
