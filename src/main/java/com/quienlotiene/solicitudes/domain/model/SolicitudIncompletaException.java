package com.quienlotiene.solicitudes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-SOL-016-02: falta un dato de la solicitud o no es válido; el mensaje dice cuál. */
public class SolicitudIncompletaException extends ReglaNegocioException {

    public SolicitudIncompletaException(String datoFaltante) {
        super(
                "SOL-002",
                "Solicitud incompleta",
                "Revisa la solicitud: " + datoFaltante + ".",
                TipoError.DATOS_INVALIDOS);
    }
}
