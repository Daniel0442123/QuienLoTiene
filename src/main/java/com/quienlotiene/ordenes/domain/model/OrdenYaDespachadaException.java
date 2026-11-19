package com.quienlotiene.ordenes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-ORD-029-02: la orden ya salió de la tienda o se entregó; el cliente ya no puede cancelarla. */
public class OrdenYaDespachadaException extends ReglaNegocioException {

    public OrdenYaDespachadaException() {
        super(
                "ORD-005",
                "La orden ya fue despachada",
                "Tu pedido ya salió de la tienda y no se puede cancelar. Escribe a la tienda o reporta un problema.",
                TipoError.CONFLICTO);
    }
}
