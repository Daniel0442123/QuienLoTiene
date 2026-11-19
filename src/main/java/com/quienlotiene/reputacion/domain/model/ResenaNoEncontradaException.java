package com.quienlotiene.reputacion.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** La orden todavía no tiene reseña, o el reporte pedido no existe. */
public class ResenaNoEncontradaException extends ReglaNegocioException {

    public ResenaNoEncontradaException(String que) {
        super("REP-003", "No encontrado", que + " no existe.", TipoError.NO_ENCONTRADO);
    }
}
