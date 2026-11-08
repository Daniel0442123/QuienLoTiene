package com.quienlotiene.tiendas.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** La tienda no hace domicilio o el cliente está más lejos que su radio de cobertura. */
public class FueraDeCoberturaException extends ReglaNegocioException {

    public FueraDeCoberturaException() {
        super(
                "TIE-010",
                "Fuera de cobertura",
                "La tienda no lleva domicilios hasta esa dirección. Puedes elegir recoger en la tienda.",
                TipoError.REGLA_NEGOCIO);
    }
}
