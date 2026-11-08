package com.quienlotiene.tiendas.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-ADM-036-02: al rechazar, la tienda debe saber por qué. */
public class MotivoRechazoInvalidoException extends ReglaNegocioException {

    public MotivoRechazoInvalidoException() {
        super(
                "TIE-005",
                "Motivo de rechazo inválido",
                "Escribe el motivo del rechazo (entre " + Tienda.LONGITUD_MINIMA_MOTIVO + " y "
                        + Tienda.LONGITUD_MAXIMA_MOTIVO + " caracteres).",
                TipoError.DATOS_INVALIDOS);
    }
}
