package com.quienlotiene.tiendas.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CU-18 paso 4: la tienda debe aceptar al menos un medio de pago. */
public class MediosPagoInvalidosException extends ReglaNegocioException {

    public MediosPagoInvalidosException() {
        super(
                "TIE-011",
                "Medios de pago inválidos",
                "Elige al menos un medio de pago: efectivo, transferencia o datáfono.",
                TipoError.DATOS_INVALIDOS);
    }
}
