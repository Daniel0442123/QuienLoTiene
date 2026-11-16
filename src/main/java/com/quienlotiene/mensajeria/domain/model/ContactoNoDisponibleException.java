package com.quienlotiene.mensajeria.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** HU-032: la orden ya no está activa, así que no se comparte el teléfono de la otra parte. */
public class ContactoNoDisponibleException extends ReglaNegocioException {

    public ContactoNoDisponibleException() {
        super(
                "MEN-001",
                "Contacto no disponible",
                "La orden ya terminó; el contacto por WhatsApp solo está disponible con la orden activa.",
                TipoError.CONFLICTO);
    }
}
