package com.quienlotiene.notificaciones.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** HU-031: la suscripción que envió el navegador no se puede usar. */
public class SuscripcionPushInvalidaException extends ReglaNegocioException {

    public SuscripcionPushInvalidaException(String detalle) {
        super(
                "NOT-001",
                "Suscripción inválida",
                "No se pudo activar el aviso: " + detalle + ".",
                TipoError.DATOS_INVALIDOS);
    }
}
