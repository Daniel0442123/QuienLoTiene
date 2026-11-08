package com.quienlotiene.tiendas.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** El archivo pesa más de 5 MB o no es JPEG, PNG, WebP ni PDF (RNF-012). */
public class ArchivoNoPermitidoException extends ReglaNegocioException {

    public ArchivoNoPermitidoException(TipoDocumento documento) {
        super(
                "TIE-003",
                "Archivo no permitido",
                "No se pudo usar " + documento.descripcion() + ": debe ser JPEG, PNG, WebP o PDF de máximo 5 MB.",
                TipoError.DATOS_INVALIDOS);
    }
}
