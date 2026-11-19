package com.quienlotiene.ordenes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-SOL-023-02: un producto restringido (HU-023) solo se recoge en la tienda. */
public class ProductoRestringidoSinDomicilioException extends ReglaNegocioException {

    public ProductoRestringidoSinDomicilioException() {
        super(
                "ORD-006",
                "Domicilio no disponible",
                "Este medicamento necesita fórmula médica: recógelo en la tienda.",
                TipoError.REGLA_NEGOCIO);
    }
}
