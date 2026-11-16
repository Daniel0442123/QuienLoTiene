package com.quienlotiene.ordenes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** La orden no existe o no es del cliente ni de la tienda que la pide (CP-ORD-024-03, CP-ORD-028-02, R-03). */
public class OrdenNoEncontradaException extends ReglaNegocioException {

    public OrdenNoEncontradaException() {
        super("ORD-001", "Orden no encontrada", "La orden no existe.", TipoError.NO_ENCONTRADO);
    }
}
