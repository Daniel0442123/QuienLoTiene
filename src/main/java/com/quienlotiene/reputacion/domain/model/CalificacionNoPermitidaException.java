package com.quienlotiene.reputacion.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-REP-034-02: solo el cliente de una orden RESUELTA puede calificar a la tienda. */
public class CalificacionNoPermitidaException extends ReglaNegocioException {

    public CalificacionNoPermitidaException() {
        super(
                "REP-001",
                "Calificación no permitida",
                "Puedes calificar a la tienda cuando confirmes que recibiste tu pedido.",
                TipoError.REGLA_NEGOCIO);
    }
}
