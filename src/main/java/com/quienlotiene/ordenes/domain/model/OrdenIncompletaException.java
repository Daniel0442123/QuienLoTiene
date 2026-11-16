package com.quienlotiene.ordenes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** Falta un dato para crear la orden; el mensaje dice cuál. */
public class OrdenIncompletaException extends ReglaNegocioException {

    public OrdenIncompletaException(String datoFaltante) {
        super("ORD-004", "Orden incompleta", "Revisa la orden: " + datoFaltante + ".", TipoError.DATOS_INVALIDOS);
    }
}
