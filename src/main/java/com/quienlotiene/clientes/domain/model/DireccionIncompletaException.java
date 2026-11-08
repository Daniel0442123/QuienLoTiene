package com.quienlotiene.clientes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-CLI-007-02: falta un dato de la dirección o no es válido; el mensaje dice cuál. */
public class DireccionIncompletaException extends ReglaNegocioException {

    public DireccionIncompletaException(String datoFaltante) {
        super(
                "CLI-001",
                "Dirección incompleta",
                "Revisa la dirección: " + datoFaltante + ".",
                TipoError.DATOS_INVALIDOS);
    }
}
