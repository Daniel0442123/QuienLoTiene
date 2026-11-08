package com.quienlotiene.clientes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** La dirección no existe o es de otro cliente (404 en ambos casos, riesgo R-03). */
public class DireccionNoEncontradaException extends ReglaNegocioException {

    public DireccionNoEncontradaException() {
        super("CLI-003", "Dirección no encontrada", "La dirección no existe.", TipoError.NO_ENCONTRADO);
    }
}
