package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-IDE-003-02: el mensaje no dice si falló el correo o la frase (diagrama 11). */
public class CredencialesIncorrectasException extends ReglaNegocioException {

    public CredencialesIncorrectasException() {
        super(
                "IDE-008",
                "Credenciales incorrectas",
                "El correo o la frase de contraseña no son correctos.",
                TipoError.NO_AUTENTICADO);
    }
}
