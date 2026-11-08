package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-IDE-003-03: solo se informa después de comprobar que el correo y la frase son correctos. */
public class CuentaSuspendidaException extends ReglaNegocioException {

    public CuentaSuspendidaException() {
        super(
                "IDE-009",
                "Cuenta suspendida",
                "Tu cuenta está suspendida. Si crees que es un error, escribe al administrador.",
                TipoError.SIN_PERMISO);
    }
}
