package com.quienlotiene.tiendas.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** El horario tiene un día repetido o una franja que abre después de cerrar (CU-16 paso 8). */
public class HorarioInvalidoException extends ReglaNegocioException {

    public HorarioInvalidoException(String problema) {
        super("TIE-007", "Horario inválido", "Revisa el horario: " + problema + ".", TipoError.DATOS_INVALIDOS);
    }
}
