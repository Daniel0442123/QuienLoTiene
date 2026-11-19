package com.quienlotiene.catalogo.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** La palabra restringida es muy corta o muy larga. */
public class PalabraRestringidaInvalidaException extends ReglaNegocioException {

    public PalabraRestringidaInvalidaException() {
        super(
                "CAT-003",
                "Palabra inválida",
                "La palabra restringida debe tener entre 2 y 80 caracteres.",
                TipoError.DATOS_INVALIDOS);
    }
}
