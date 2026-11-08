package com.quienlotiene.tiendas.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CP-TIE-002-02: falta un documento obligatorio; el mensaje dice cuál. */
public class DocumentoFaltanteException extends ReglaNegocioException {

    public DocumentoFaltanteException(TipoDocumento documento) {
        super("TIE-002", "Documento faltante", "Adjunta " + documento.descripcion() + ".", TipoError.DATOS_INVALIDOS);
    }
}
