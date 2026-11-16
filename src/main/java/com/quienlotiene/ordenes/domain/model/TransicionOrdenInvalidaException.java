package com.quienlotiene.ordenes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-ORD-027-02, RNF-016: la orden no puede pasar a ese estado desde el actual. */
public class TransicionOrdenInvalidaException extends ReglaNegocioException {

    public TransicionOrdenInvalidaException(EstadoOrden actual, EstadoOrden pedido) {
        super(
                "ORD-002",
                "Cambio de estado no permitido",
                "La orden está " + actual.textoParaUsuario() + " y no puede pasar a " + pedido.textoParaUsuario() + ".",
                TipoError.CONFLICTO);
    }
}
