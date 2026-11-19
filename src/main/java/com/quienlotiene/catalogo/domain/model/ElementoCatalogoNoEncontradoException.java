package com.quienlotiene.catalogo.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** La categoría o la palabra restringida no existe. */
public class ElementoCatalogoNoEncontradoException extends ReglaNegocioException {

    public ElementoCatalogoNoEncontradoException(String que) {
        super("CAT-001", "No encontrado", que + " no existe.", TipoError.NO_ENCONTRADO);
    }
}
