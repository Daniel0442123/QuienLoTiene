package com.quienlotiene.tiendas.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** La tienda o el documento pedido no existe. */
public class TiendaNoEncontradaException extends ReglaNegocioException {

    public TiendaNoEncontradaException() {
        super("TIE-006", "Tienda no encontrada", "La tienda o el documento no existe.", TipoError.NO_ENCONTRADO);
    }
}
