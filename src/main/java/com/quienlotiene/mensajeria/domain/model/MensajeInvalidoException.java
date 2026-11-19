package com.quienlotiene.mensajeria.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** HU-033: el mensaje está vacío o es demasiado largo. */
public class MensajeInvalidoException extends ReglaNegocioException {

    public MensajeInvalidoException() {
        super(
                "MEN-003",
                "Mensaje inválido",
                "Escribe un mensaje de 1 a " + Mensaje.LARGO_MAXIMO + " caracteres.",
                TipoError.DATOS_INVALIDOS);
    }
}
