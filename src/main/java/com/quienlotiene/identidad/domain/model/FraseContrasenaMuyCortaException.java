package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-IDE-001-03: la frase de contraseña tiene menos de 15 caracteres. */
public class FraseContrasenaMuyCortaException extends ReglaNegocioException {

    public FraseContrasenaMuyCortaException() {
        super(
                "IDE-002",
                "Frase de contraseña muy corta",
                "La frase de contraseña debe tener al menos " + FraseContrasena.LONGITUD_MINIMA
                        + " caracteres. Prueba con varias palabras, por ejemplo \"mi perro come mango\".",
                TipoError.DATOS_INVALIDOS);
    }
}
