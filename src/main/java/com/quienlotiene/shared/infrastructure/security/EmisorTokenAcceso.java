package com.quienlotiene.shared.infrastructure.security;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * Firma los tokens de acceso con RS256 (D-15). El {@code sub} es el id del usuario y el claim {@value #CLAIM_ROLES}
 * lleva sus roles ({@code CLIENTE}, {@code TIENDA}, {@code ADMIN}). El módulo {@code identidad} lo usa al iniciar
 * sesión y al refrescar la sesión.
 */
@Component
public class EmisorTokenAcceso {

    public static final String CLAIM_ROLES = "roles";

    private final JwtEncoder codificadorJwt;
    private final PropiedadesJwt propiedades;
    private final Clock reloj;

    public EmisorTokenAcceso(JwtEncoder codificadorJwt, PropiedadesJwt propiedades, Clock reloj) {
        this.codificadorJwt = codificadorJwt;
        this.propiedades = propiedades;
        this.reloj = reloj;
    }

    public TokenAccesoEmitido emitir(UUID usuarioId, Collection<String> roles) {
        var ahora = reloj.instant();
        var fechaExpiracion = ahora.plus(propiedades.vigenciaTokenAcceso());
        var datosToken = JwtClaimsSet.builder()
                .issuer(propiedades.emisor())
                .subject(usuarioId.toString())
                .issuedAt(ahora)
                .expiresAt(fechaExpiracion)
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_ROLES, List.copyOf(roles))
                .build();
        var encabezado = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(propiedades.kid())
                .build();
        var valor = codificadorJwt
                .encode(JwtEncoderParameters.from(encabezado, datosToken))
                .getTokenValue();
        return new TokenAccesoEmitido(valor, fechaExpiracion);
    }
}
