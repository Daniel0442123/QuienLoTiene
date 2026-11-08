package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** La frase supera 128 caracteres; el tope evita que una frase enorme vuelva lento el cálculo del hash. */
public class FraseContrasenaMuyLargaException extends ReglaNegocioException {

    public FraseContrasenaMuyLargaException() {
        super(
                "IDE-004",
                "Frase de contraseña muy larga",
                "La frase de contraseña puede tener máximo " + FraseContrasena.LONGITUD_MAXIMA + " caracteres.",
                TipoError.DATOS_INVALIDOS);
    }
}
