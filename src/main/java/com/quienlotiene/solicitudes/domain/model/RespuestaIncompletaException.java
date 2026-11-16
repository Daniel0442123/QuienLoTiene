package com.quienlotiene.solicitudes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-SOL-019-02: falta un dato de la respuesta o no es válido; el mensaje dice cuál. */
public class RespuestaIncompletaException extends ReglaNegocioException {

    public RespuestaIncompletaException(String datoFaltante) {
        super(
                "SOL-006",
                "Respuesta incompleta",
                "Revisa la respuesta: " + datoFaltante + ".",
                TipoError.DATOS_INVALIDOS);
    }
}
