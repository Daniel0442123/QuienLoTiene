package com.quienlotiene.tiendas.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** Falta un dato de la tienda o no es válido; el mensaje dice cuál. */
public class TiendaIncompletaException extends ReglaNegocioException {

    public TiendaIncompletaException(String datoFaltante) {
        super(
                "TIE-001",
                "Tienda incompleta",
                "Revisa los datos de la tienda: " + datoFaltante + ".",
                TipoError.DATOS_INVALIDOS);
    }
}
