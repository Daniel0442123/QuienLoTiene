package com.quienlotiene.tiendas.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** CU-19 paso 5: solo una tienda APROBADA puede abrir o cerrar. */
public class TiendaNoAprobadaException extends ReglaNegocioException {

    public TiendaNoAprobadaException() {
        super(
                "TIE-008",
                "Tienda no aprobada",
                "Tu tienda aún no está aprobada. Podrás abrirla cuando el administrador revise tus documentos.",
                TipoError.CONFLICTO);
    }
}
