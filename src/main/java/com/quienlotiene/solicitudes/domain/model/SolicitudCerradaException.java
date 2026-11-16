package com.quienlotiene.solicitudes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-SOL-021-01: la solicitud ya no está ABIERTA o venció; no acepta respuestas ni elecciones. */
public class SolicitudCerradaException extends ReglaNegocioException {

    public SolicitudCerradaException() {
        super(
                "SOL-004",
                "Solicitud cerrada",
                "La solicitud ya no recibe respuestas: venció o el cliente ya eligió una.",
                TipoError.CONFLICTO);
    }
}
