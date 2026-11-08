package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-IDE-001-03: la frase aparece en la lista de contraseñas comunes o filtradas (NIST 800-63B). */
public class FraseContrasenaComunException extends ReglaNegocioException {

    public FraseContrasenaComunException() {
        super(
                "IDE-003",
                "Frase de contraseña común",
                "Esa frase de contraseña es muy común o apareció en filtraciones conocidas. Elige otra.",
                TipoError.DATOS_INVALIDOS);
    }
}
