package com.quienlotiene.ordenes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-ORD-024-04: la tienda no tiene activo el medio de pago elegido. */
public class MedioPagoNoAceptadoException extends ReglaNegocioException {

    public MedioPagoNoAceptadoException() {
        super(
                "ORD-003",
                "Medio de pago no aceptado",
                "La tienda no recibe ese medio de pago. Elige otro.",
                TipoError.REGLA_NEGOCIO);
    }
}
