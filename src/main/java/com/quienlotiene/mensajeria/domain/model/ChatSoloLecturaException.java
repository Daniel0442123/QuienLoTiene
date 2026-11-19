package com.quienlotiene.mensajeria.domain.model;

import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;

/** HU-033, CP-MEN-033-02: la orden terminó, así que su chat queda en solo lectura. */
public class ChatSoloLecturaException extends ReglaNegocioException {

    public ChatSoloLecturaException() {
        super(
                "MEN-002",
                "Chat en solo lectura",
                "La orden ya terminó; puedes leer la conversación pero no escribir.",
                TipoError.CONFLICTO);
    }
}
