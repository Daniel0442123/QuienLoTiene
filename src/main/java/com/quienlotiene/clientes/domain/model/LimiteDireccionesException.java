package com.quienlotiene.clientes.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** El cliente ya tiene el máximo de direcciones guardadas. */
public class LimiteDireccionesException extends ReglaNegocioException {

    public LimiteDireccionesException() {
        super(
                "CLI-002",
                "Límite de direcciones",
                "Puedes guardar hasta " + Direccion.MAXIMO_POR_CLIENTE + " direcciones. Borra una para agregar otra.",
                TipoError.REGLA_NEGOCIO);
    }
}
