package com.quienlotiene.solicitudes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** La solicitud no existe, es de otro cliente o no llegó a la tienda que la pide (R-03). */
public class SolicitudNoEncontradaException extends ReglaNegocioException {

    public SolicitudNoEncontradaException() {
        super(
                "SOL-003",
                "Solicitud no encontrada",
                "La solicitud no existe o no está disponible para ti.",
                TipoError.NO_ENCONTRADO);
    }
}
