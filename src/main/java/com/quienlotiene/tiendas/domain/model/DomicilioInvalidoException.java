package com.quienlotiene.tiendas.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-TIE-010-02: la tarifa es negativa o el radio de cobertura está fuera de rango (CU-17 paso 3). */
public class DomicilioInvalidoException extends ReglaNegocioException {

    public DomicilioInvalidoException(String problema) {
        super("TIE-009", "Domicilio inválido", "Revisa el domicilio: " + problema + ".", TipoError.DATOS_INVALIDOS);
    }
}
