package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-IDE-001-02: ya existe una cuenta con ese correo. */
public class CorreoYaRegistradoException extends ReglaNegocioException {

    public CorreoYaRegistradoException() {
        super(
                "IDE-001",
                "Correo ya registrado",
                "Ese correo ya tiene una cuenta. Inicia sesión o usa otro correo.",
                TipoError.CONFLICTO);
    }
}
