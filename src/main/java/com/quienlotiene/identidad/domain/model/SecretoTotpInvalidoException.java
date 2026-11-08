package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** El secreto TOTP no es Base32 o es más corto que 128 bits (RFC 4226 recomienda 160). */
public class SecretoTotpInvalidoException extends ReglaNegocioException {

    public SecretoTotpInvalidoException() {
        super(
                "IDE-014",
                "Secreto TOTP inválido",
                "El secreto TOTP debe estar en Base32 (letras A-Z y dígitos 2-7) y tener al menos 26 caracteres.",
                TipoError.DATOS_INVALIDOS);
    }
}
