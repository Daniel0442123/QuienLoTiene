package com.quienlotiene.tiendas.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** Solo una tienda PENDIENTE se aprueba o se rechaza (diagrama 05-estados). */
public class TransicionTiendaInvalidaException extends ReglaNegocioException {

    public TransicionTiendaInvalidaException(String accion, EstadoTienda estadoActual) {
        super(
                "TIE-004",
                "Cambio de estado no permitido",
                "No se puede " + accion + " una tienda en estado " + estadoActual + ".",
                TipoError.CONFLICTO);
    }
}
