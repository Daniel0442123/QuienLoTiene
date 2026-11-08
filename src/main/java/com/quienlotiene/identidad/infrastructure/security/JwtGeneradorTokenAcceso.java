package com.quienlotiene.identidad.infrastructure.security;

import com.quienlotiene.identidad.domain.model.Rol;
import com.quienlotiene.identidad.domain.model.TokenAcceso;
import com.quienlotiene.identidad.domain.port.GeneradorTokenAcceso;
import com.quienlotiene.shared.infrastructure.security.EmisorTokenAcceso;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador que firma el JWT con el emisor RS256 de {@code shared} (T-02). */
@Component
class JwtGeneradorTokenAcceso implements GeneradorTokenAcceso {

    private final EmisorTokenAcceso emisorTokenAcceso;

    JwtGeneradorTokenAcceso(EmisorTokenAcceso emisorTokenAcceso) {
        this.emisorTokenAcceso = emisorTokenAcceso;
    }

    @Override
    public TokenAcceso generar(UUID usuarioId, Rol rol) {
        var tokenEmitido = emisorTokenAcceso.emitir(usuarioId, List.of(rol.name()));
        return new TokenAcceso(tokenEmitido.valor(), tokenEmitido.fechaExpiracion());
    }
}
