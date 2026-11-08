package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** El correo no tiene la forma usuario@dominio o supera 254 caracteres. */
public class CorreoInvalidoException extends ReglaNegocioException {

    public CorreoInvalidoException() {
        super(
                "IDE-005",
                "Correo inválido",
                "El correo no es válido. Revisa que tenga la forma nombre@dominio.com.",
                TipoError.DATOS_INVALIDOS);
    }
}
