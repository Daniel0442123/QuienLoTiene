package com.quienlotiene.reputacion.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** La reseña o el reporte llegan con datos que no cumplen las reglas; el mensaje dice cuál. */
public class CalificacionInvalidaException extends ReglaNegocioException {

    public CalificacionInvalidaException(String detalle) {
        super("REP-004", "Datos inválidos", "Revisa los datos: " + detalle + ".", TipoError.DATOS_INVALIDOS);
    }
}
