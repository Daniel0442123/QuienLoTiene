package com.quienlotiene.identidad.domain.port;

import com.quienlotiene.identidad.domain.model.Rol;
import com.quienlotiene.identidad.domain.model.TokenAcceso;
import java.util.UUID;

/** Firma el token de acceso de 15 minutos con el id y el rol del usuario. */
public interface GeneradorTokenAcceso {

    TokenAcceso generar(UUID usuarioId, Rol rol);
}
