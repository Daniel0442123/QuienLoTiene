package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-IDE-004-02: el código TOTP está vencido o es incorrecto. */
public class CodigoSegundoFactorIncorrectoException extends ReglaNegocioException {

    public CodigoSegundoFactorIncorrectoException() {
        super(
                "IDE-012",
                "Código incorrecto",
                "El código de verificación no es correcto o ya venció. Escribe el que muestra ahora tu aplicación.",
                TipoError.NO_AUTENTICADO);
    }
}
