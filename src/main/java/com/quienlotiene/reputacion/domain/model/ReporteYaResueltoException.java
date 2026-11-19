package com.quienlotiene.reputacion.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** HU-037: el reporte ya está resuelto. */
public class ReporteYaResueltoException extends ReglaNegocioException {

    public ReporteYaResueltoException() {
        super("REP-005", "Reporte ya resuelto", "Este reporte ya fue resuelto.", TipoError.CONFLICTO);
    }
}
