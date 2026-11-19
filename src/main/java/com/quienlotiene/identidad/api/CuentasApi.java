package com.quienlotiene.identidad.api;

import java.util.UUID;

/** Fachada para que administracion suspenda o reactive la cuenta del dueño de una tienda (HU-037, CU-27). */
public interface CuentasApi {

    /** La cuenta queda SUSPENDIDA y se cierran sus sesiones: no puede iniciar sesión ni refrescar (IDE-009). */
    void suspender(UUID usuarioId);

    /** La cuenta vuelve a ACTIVA; el usuario inicia sesión de nuevo. */
    void reactivar(UUID usuarioId);
}
