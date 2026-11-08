package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-IDE-005-01: el visitante no aceptó la política de tratamiento de datos. */
public class AutorizacionDatosRequeridaException extends ReglaNegocioException {

    public AutorizacionDatosRequeridaException() {
        super(
                "IDE-006",
                "Autorización de datos requerida",
                "Para crear la cuenta debes aceptar la política de tratamiento de datos personales.",
                TipoError.DATOS_INVALIDOS);
    }
}
