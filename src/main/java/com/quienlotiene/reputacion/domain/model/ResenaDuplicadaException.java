package com.quienlotiene.reputacion.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-REP-034-03: la orden ya tiene reseña. */
public class ResenaDuplicadaException extends ReglaNegocioException {

    public ResenaDuplicadaException() {
        super("REP-002", "Ya calificaste", "Ya calificaste a la tienda por este pedido.", TipoError.CONFLICTO);
    }
}
