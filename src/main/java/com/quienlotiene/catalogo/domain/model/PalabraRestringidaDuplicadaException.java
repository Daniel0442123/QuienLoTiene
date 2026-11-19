package com.quienlotiene.catalogo.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** La palabra ya está en la lista de restringidos. */
public class PalabraRestringidaDuplicadaException extends ReglaNegocioException {

    public PalabraRestringidaDuplicadaException() {
        super(
                "CAT-002",
                "Ya está en la lista",
                "Esa palabra ya está en la lista de restringidos.",
                TipoError.CONFLICTO);
    }
}
