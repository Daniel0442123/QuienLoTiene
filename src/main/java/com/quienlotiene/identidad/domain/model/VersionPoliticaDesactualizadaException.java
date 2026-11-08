package com.quienlotiene.identidad.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** La versión de la política que el visitante aceptó no es la vigente: la pantalla debe mostrar la nueva. */
public class VersionPoliticaDesactualizadaException extends ReglaNegocioException {

    public VersionPoliticaDesactualizadaException(String versionVigente) {
        super(
                "IDE-007",
                "Versión de la política desactualizada",
                "La política de tratamiento de datos cambió. Lee y acepta la versión " + versionVigente + ".",
                TipoError.CONFLICTO);
    }
}
