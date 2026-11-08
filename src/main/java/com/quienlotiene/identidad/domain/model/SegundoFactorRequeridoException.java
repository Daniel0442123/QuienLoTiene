package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** El administrador entra por CU-25 con código TOTP (HU-004); el inicio de sesión normal no le da acceso. */
public class SegundoFactorRequeridoException extends ReglaNegocioException {

    public SegundoFactorRequeridoException() {
        super(
                "IDE-011",
                "Segundo factor requerido",
                "Esta cuenta necesita un código de verificación para iniciar sesión.",
                TipoError.NO_AUTENTICADO);
    }
}
